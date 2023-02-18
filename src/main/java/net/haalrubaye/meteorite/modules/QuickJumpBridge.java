package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;
import net.minecraft.client.option.KeyBinding;

public class QuickJumpBridge extends Module {

    public enum Mode {
        Hold,
        Press
    }

    public enum Button {
        Right,
        Left
    }

    public enum LockMode {
        Smart,
        Simple,
        None
    }

    private final SettingGroup sgGeneral = settings.createGroup("Clicker");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The method of clicking.")
        .defaultValue(Mode.Press)
        .build()
    );

    private final Setting<Button> button = sgGeneral.add(new EnumSetting.Builder<Button>()
        .name("button")
        .description("Which button to press.")
        .defaultValue(Button.Right)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("click-delay")
        .description("The amount of delay between clicks in ticks.")
        .defaultValue(2)
        .min(0)
        .sliderMax(60)
        .build()
    );

    public QuickJumpBridge() {
        super(Addon.CATEGORY, "QuickJumpBridge", "Automatically bridgejumps in the direction you are looking.");
    }

    private int timer;
    private float yawDirection;

    boolean turn;
    int blockCount;

    @Override
    public void onActivate() {
        turn = true;
        blockCount = 0;
        yawDirection = getSmartYawDirection();
        timer = 0;
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
    }

    @Override
    public void onDeactivate() {
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
        unpress();
        mc.options.sneakKey.setPressed(false);
    }

    private final SettingGroup sgYaw = settings.createGroup("Yaw");
    private final SettingGroup sgPitch = settings.createGroup("Pitch");

    // Yaw

    private final Setting<LockMode> yawLockMode = sgYaw.add(new EnumSetting.Builder<LockMode>()
        .name("yaw-lock-mode")
        .description("The way in which your yaw is locked.")
        .defaultValue(LockMode.Simple)
        .build()
    );

    private final Setting<Double> yawAngle = sgYaw.add(new DoubleSetting.Builder()
        .name("yaw-angle")
        .description("Yaw angle in degrees.")
        .defaultValue(0)
        .sliderMax(360)
        .max(360)
        .build()
    );

    // Pitch

    private final Setting<LockMode> pitchLockMode = sgPitch.add(new EnumSetting.Builder<LockMode>()
        .name("pitch-lock-mode")
        .description("The way in which your pitch is locked.")
        .defaultValue(LockMode.Simple)
        .build()
    );

    private final Setting<Double> pitchAngle = sgPitch.add(new DoubleSetting.Builder()
        .name("pitch-angle")
        .description("Pitch angle in degrees.")
        .defaultValue(0)
        .range(-90, 90)
        .sliderRange(-90, 90)
        .build()
    );

    @EventHandler

    private void onTick(TickEvent.Pre event) {

        switch (yawLockMode.get()) {
            case Simple -> setYawAngle(yawAngle.get().floatValue());
            case Smart  -> setYawAngle(yawDirection);
        }

        switch (pitchLockMode.get()) {
            case Simple -> mc.player.setPitch(pitchAngle.get().floatValue());
            case Smart  -> mc.player.setPitch(getSmartPitchDirection());
        }



        switch (mode.get()) {
            case Hold:
                switch (button.get()) {
                    case Left -> mc.options.attackKey.setPressed(true);
                    case Right -> mc.options.useKey.setPressed(true);
                }
                break;
            case Press:
                timer++;
                if (!(delay.get() > timer)) {
                    switch (button.get()) {
                        case Left -> Utils.leftClick();
                        case Right -> Utils.rightClick();
                    }
                    timer = 0;
                }
                break;
        }

        if (mc.player.isOnGround()){
            mc.player.jump();
        }

        setPressed(mc.options.backKey, true);

        // rotate to specific angle
        // walk back
        // autoclick



    }

    private float getSmartYawDirection() {
        float yaw = Math.round((mc.player.getYaw() + 1f) / 45f) * 45f;
//        yaw += 180f;
//        if (yaw >= 360f){
//            return (yaw - 360f);
//        }
        return yaw;
    }

    private float getSmartPitchDirection() {
        return Math.round((mc.player.getPitch() + 1f) / 30f) * 30f;
    }

    private void setYawAngle(float yawAngle) {
        mc.player.setYaw(yawAngle);
        mc.player.headYaw = yawAngle;
        mc.player.bodyYaw = yawAngle;
    }

    private void unpress() {
        setPressed(mc.options.backKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
}
