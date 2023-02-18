package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.haalrubaye.meteorite.Addon;


public class Teleporter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> x = sgGeneral.add(new IntSetting.Builder()
            .name("x-axis")
            .description("Sets the x-axis.")
            .defaultValue(0)
            .build()
    );

    private final Setting<Integer> y = sgGeneral.add(new IntSetting.Builder()
            .name("y-axis")
            .description("Sets the y-axis.")
            .defaultValue(0)
            .build()
    );

    private final Setting<Integer> z = sgGeneral.add(new IntSetting.Builder()
            .name("z-axis")
            .description("Sets the z-axis.")
            .defaultValue(0)
            .build()
    );

    public Teleporter() {
        super(Addon.CATEGORY, "teleporter", "Allows you to teleport.");
    }

    @Override
    public void onActivate() {
        mc.player.setPosition(x.get(), y.get(), z.get());
        toggle();
    }
}
