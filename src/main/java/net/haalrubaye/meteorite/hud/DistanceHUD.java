package net.haalrubaye.meteorite.hud;

import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.haalrubaye.meteorite.Addon;
import net.haalrubaye.meteorite.modules.Distance;

public class DistanceHUD extends HudElement {
    public static final HudElementInfo<DistanceHUD> INFO = new HudElementInfo<>(Addon.HUD_GROUP, "distance", "HUD distance element.", DistanceHUD::new);

    public DistanceHUD() {
        super(INFO);
    }

    String distance;

    @Override
    public void render(HudRenderer renderer) {
        distance = Distance.getDistance();

        setSize(renderer.textWidth("Distance travelled: " + distance, true), renderer.textHeight(true));

        renderer.text("Distance travelled: " + distance, x, y, Color.WHITE, true);
    }
}
