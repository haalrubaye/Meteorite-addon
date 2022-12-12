package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import net.haalrubaye.meteorite.Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import net.minecraft.client.option.KeyBinding;
import meteordevelopment.meteorclient.utils.misc.input.Input;

public class JumpBridge extends Module {

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

    public JumpBridge() {
        super(Addon.CATEGORY, "JumpBridge", "Automatically bridges in the direction you are looking, but occasionally jumps.");
    }

    private int timer;
    private float yawDirection;

    @Override
    public void onActivate() {
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

    private final SettingGroup jumpPeriod = settings.createGroup("Jump");

    private final Setting<Integer> jump = jumpPeriod.add(new IntSetting.Builder()
        .name("jump-period")
        .description("The number of blocks between a jump.")
        .defaultValue(6)
        .min(2)
        .sliderMax(40)
        .build()
    );

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

    boolean turn = true;
    int blockCount = 0;
    @EventHandler

    private void onTick(TickEvent.Pre event) {
        if (mc.world.getBlockState(mc.player.getSteppingPos()).isAir()) {
            if (mc.player.isOnGround()) {
                turn = true;
                mc.options.sneakKey.setPressed(true);
            }
        } else if (turn) {
            turn = false;
            blockCount += 1;
            mc.options.sneakKey.setPressed(false);
            if (blockCount == jump.get()){
                blockCount = 0;
                mc.player.jump();
            }
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

        switch (yawLockMode.get()) {
            case Simple -> setYawAngle(yawAngle.get().floatValue());
            case Smart  -> setYawAngle(yawDirection);
        }

        switch (pitchLockMode.get()) {
            case Simple -> mc.player.setPitch(pitchAngle.get().floatValue());
            case Smart  -> mc.player.setPitch(getSmartPitchDirection());
        }

        setPressed(mc.options.backKey, true);

        // rotate to specific angle
        // walk back
        // autoclick



    }

    private float getSmartYawDirection() {
        float yaw = Math.round((mc.player.getYaw() + 1f) / 90f) * 90f;
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
