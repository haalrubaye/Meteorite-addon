package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.haalrubaye.meteorite.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

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
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) mc.crosshairTarget).getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                timer = 0;
            }
        }
    }
}
