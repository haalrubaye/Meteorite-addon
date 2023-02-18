package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class OnHoldClicker extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("click-delay")
        .description("The amount of delay between clicks in ticks.")
        .defaultValue(2)
        .min(0)
        .sliderMax(60)
        .build()
    );

    private int timer;

    public OnHoldClicker() {
        super(Addon.CATEGORY, "onHold-clicker", "Clicks while the left mouse button is pressed.");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.options.attackKey.isPressed()) {
            timer++;
            if (!(delay.get() > timer)) {
                if (mc.crosshairTarget == null) return;
                if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
                if (!(validWeapon(mc.player.getMainHandStack().getItem()))){
                    boolean found = false;
                    int i = 0;

                    while (!found && i < 9) {
                        found = validWeapon(mc.player.getInventory().getStack(i).getItem());
                        i++;
                    }

                    if (found) {
                        InvUtils.swap(i - 1, false);
                    } else return;
                }
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) mc.crosshairTarget).getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                timer = 0;
            }
        }
    }

    private boolean validWeapon(Item item){
        if (item instanceof SwordItem) return true;
        return false;
    }
}
