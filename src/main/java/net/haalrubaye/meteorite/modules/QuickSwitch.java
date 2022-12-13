package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.haalrubaye.meteorite.Addon;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;


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

        if (mc.options.useKey.isPressed() && !swapped && mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            boolean found = false;
            int i = 0;

            while (!found && i < 9) {
                found = validItem(mc.player.getInventory().getStack(i));
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
    private boolean validItem(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        return blocks.get().contains(block);
    }
}
