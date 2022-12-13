package net.haalrubaye.meteorite;

import net.haalrubaye.meteorite.commands.CommandExample;
import net.haalrubaye.meteorite.hud.HudExample;
import net.haalrubaye.meteorite.modules.AutoBridge;
import net.haalrubaye.meteorite.modules.BlockPath;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.haalrubaye.meteorite.modules.JumpBridge;
import net.haalrubaye.meteorite.modules.QuickSwitch;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Meteorite");
    public static final HudGroup HUD_GROUP = new HudGroup("Meteorite");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");

        // Modules
        Modules.get().add(new AutoBridge());
        Modules.get().add(new JumpBridge());
        Modules.get().add(new BlockPath());
        Modules.get().add(new QuickSwitch());

        // Commands
        Commands.get().add(new CommandExample());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "net.haalrubaye.meteorite";
    }
}
