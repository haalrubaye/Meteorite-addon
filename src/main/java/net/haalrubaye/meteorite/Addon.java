package net.haalrubaye.meteorite;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.haalrubaye.meteorite.commands.CommandExample;
import net.haalrubaye.meteorite.hud.DistanceHUD;
import net.haalrubaye.meteorite.modules.*;
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
        Modules.get().add(new OnHoldClicker());
        Modules.get().add(new BlockIn());
        Modules.get().add(new Distance());
        Modules.get().add(new VelocityPlus());
        Modules.get().add(new QuickJumpBridge());
        Modules.get().add(new NewChunks());
        Modules.get().add(new OccasionalJump());
        Modules.get().add(new Teleporter());

        // Commands
        Commands.get().add(new CommandExample());

        // HUD
        Hud.get().register(DistanceHUD.INFO);
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
