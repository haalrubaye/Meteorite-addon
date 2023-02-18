package net.haalrubaye.meteorite.modules;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BlockPath extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Rotation speed");

    // General

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Selected blocks.")
        .build()
    );

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to block.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range at which an entity can be targeted.")
        .defaultValue(5)
        .min(0)
        .build()
    );

    private final Setting<Boolean> ignoreWalls = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-walls")
        .description("Whether or not to ignore aiming through walls.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to select target from entities in range.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );

    // Aim Speed

    private final Setting<Boolean> instant = sgSpeed.add(new BoolSetting.Builder()
        .name("instant-look")
        .description("Instantly rotates.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("speed")
        .description("How fast to rotate.")
        .defaultValue(5)
        .min(0)
        .visible(() -> !instant.get())
        .build()
    );

    private final Setting<Integer> distance = sgSpeed.add(new IntSetting.Builder()
        .name("distance")
        .description("Distance in front of player to place block.")
        .defaultValue(2)
        .min(2)
        .sliderMax(8)
        .build()
    );

    private final Vec3 vec3d1 = new Vec3();
    private Entity target;

    private boolean clicked;

    public BlockPath() {
        super(Addon.CATEGORY, "path-blocker", "Blocks path of movement of Entity with a block.");
    }

    @Override
    public void onActivate() {
        clicked = false;
        boolean found = false;
        int i = 0;

        while (!found && i < 9) {
            found = validItem(mc.player.getInventory().getStack(i));
            i++;
        }
        if (found) {
            InvUtils.swap(i -1, true);
        } else {
            toggle();
        }
    }


    @EventHandler
    private void onTick(TickEvent.Post event) {

        target = TargetUtils.get(entity -> {
            if (!entity.isAlive()) return false;
            if (mc.player.distanceTo(entity) >= range.get()) return false;
            if (!ignoreWalls.get() && !PlayerUtils.canSeeEntity(entity)) return false;
            if (entity == mc.player || !entities.get().getBoolean(entity.getType())) return false;
            if (entity instanceof PlayerEntity) {
                return Friends.get().shouldAttack((PlayerEntity) entity);
            }
            return true;
        }, priority.get());

        if (clicked) {
            InvUtils.swapBack();
            toggle();
        }

        if (!clicked && aimingAtSpot()) {
            Utils.rightClick();
            clicked = true;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target != null){
            aim(target, event.tickDelta, instant.get());

        }
    }

    private void aim(Entity target, double delta, boolean instant) {
        // Entity angleOfSight

        double entityYaw;
        if (target instanceof PlayerEntity){
            entityYaw = (double) ((PlayerEntity) target).headYaw * (Math.PI/180);
        } else {
            entityYaw = (double) target.getYaw() * (Math.PI/180);
        }

        vec3d1.set(target, delta);

        if (entityYaw <= -90f){
            vec3d1.add( Math.sin(entityYaw + Math.PI) * distance.get(), 0, Math.cos(entityYaw + Math.PI) * -1 * distance.get());
        } else if (entityYaw <= 0f) {
            vec3d1.add(Math.sin(entityYaw * -1) * distance.get(), 0, Math.cos(entityYaw * -1) * distance.get());
        } else if (entityYaw <= 90f) {
            vec3d1.add(Math.sin(entityYaw) * -1 * distance.get(), 0, Math.cos(entityYaw) * distance.get());
        } else if (entityYaw <= 180f) {
            vec3d1.add(Math.sin((entityYaw - Math.PI) * -1) * -1 * distance.get(), 0, Math.cos((entityYaw - Math.PI) * -1) * -1 * distance.get());
        }

        double deltaX = vec3d1.x - mc.player.getX();
        double deltaZ = vec3d1.z - mc.player.getZ();
        double deltaY = vec3d1.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));

        // Yaw
        double angle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        double deltaAngle;
        double toRotate;

        if (instant) {
            mc.player.setYaw((float) angle);
        } else {
            deltaAngle = MathHelper.wrapDegrees(angle - mc.player.getYaw());
            toRotate = speed.get() * (deltaAngle >= 0 ? 1 : -1) * delta;
            if ((toRotate >= 0 && toRotate > deltaAngle) || (toRotate < 0 && toRotate < deltaAngle)) toRotate = deltaAngle;
            mc.player.setYaw(mc.player.getYaw() + (float) toRotate);
        }

        // Pitch
        double idk = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        angle = -Math.toDegrees(Math.atan2(deltaY, idk));

        if (instant) {
            mc.player.setPitch((float) angle);
        } else {
            deltaAngle = MathHelper.wrapDegrees(angle - mc.player.getPitch());
            toRotate = speed.get() * (deltaAngle >= 0 ? 1 : -1) * delta;
            if ((toRotate >= 0 && toRotate > deltaAngle) || (toRotate < 0 && toRotate < deltaAngle))
                toRotate = deltaAngle;
            mc.player.setPitch(mc.player.getPitch() + (float) toRotate);
        }
    }

    private boolean validItem(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        return blocks.get().contains(block);
    }

    private boolean aimingAtSpot(){
        Vec3d primary = mc.crosshairTarget.getPos();
        return distanceBetween(primary, vec3d1) < 0.2;
    }
    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    private double distanceBetween(Vec3d vec1, Vec3 vec2){
        float f = (float) (vec1.x - vec2.x);
        float g = (float) (vec1.y - vec2.y);
        float h = (float) (vec1.z - vec2.z);
        return MathHelper.sqrt(f * f + g * g + h * h);
    }
}
