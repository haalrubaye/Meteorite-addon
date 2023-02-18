package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;
import net.minecraft.util.math.Vec3d;

public class Distance extends Module {
    public Distance() {
        super(Addon.CATEGORY, "distance", "Displays the distance elapsed during travel.");
    }

    static String distance = "0";
    Vec3d initialPos;
    Vec3d finalPos;

    public void onActivate() {
        initialPos = mc.player.getPos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        finalPos = mc.player.getPos();
        evaluateDistance();
    }

    private void evaluateDistance() {
        distance = String.format("%.1f", finalPos.distanceTo(initialPos));
    }

    public static String getDistance(){
        return distance;
    }
}
