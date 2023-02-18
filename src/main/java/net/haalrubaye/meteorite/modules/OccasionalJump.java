package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;


public class OccasionalJump extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
            .name("height-limit")
            .description("Minimum altitude before jump.")
            .defaultValue(122)
            .min(-63)
            .sliderMax(311)
            .build()
    );

    public OccasionalJump() {
        super(Addon.CATEGORY, "occasional-jump", "Jumps when below given block.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.getY() < (height.get() + 0.05)){
            mc.player.setPosition(mc.player.getX(), height.get()+0.4, mc.player.getZ());
        }
    }
}
