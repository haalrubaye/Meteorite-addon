package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.util.hit.HitResult;

import java.util.List;

public class QuickSwitch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Selected blocks.")
        .build()
    );

    private int delayBeforeClick;
    private int delayBeforeSwapBack;

    private boolean swapped;

    public QuickSwitch() {
        super(Addon.CATEGORY, "quick-switcher", "Switches between sword and blocks for PVP combat.");
    }

    @Override
    public void onActivate() {
        swapped = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {

        if (mc.options.useKey.isPressed() && !swapped && validTool(mc.player.getMainHandStack().getItem())) {
            boolean found = false;
            int i = 0;

            HitResult.Type type = mc.crosshairTarget.getType();

            while (!found && i < 9) {
                if (type == HitResult.Type.BLOCK) found = validBlock(mc.player.getInventory().getStack(i));
                else found = validProjectile(mc.player.getInventory().getStack(i).getItem());
                i++;
            }
            if (found) {
                InvUtils.swap(i - 1, true);
                delayBeforeClick = 1;
                delayBeforeSwapBack = 3;
                swapped = true;
            }
        }

        if (swapped) {
            if (delayBeforeSwapBack == 0){
                InvUtils.swapBack();
                swapped = false;
            }
            if (delayBeforeClick == 0) {
                Utils.rightClick();
            }

            delayBeforeClick--;
            delayBeforeSwapBack--;
        }



    }
    private boolean validBlock(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        return blocks.get().contains(block);
    }

    private boolean validProjectile(Item item){
        if (item instanceof SnowballItem || item instanceof EggItem || item instanceof FishingRodItem || item instanceof EnderPearlItem){
            return true;
        }
        return false;
    }

    private boolean validTool(Item item){
        if (item instanceof SwordItem || item instanceof ToolItem || item instanceof ShearsItem){
            return true;
        }
        return false;
    }
}
