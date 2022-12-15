package net.haalrubaye.meteorite.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import net.haalrubaye.meteorite.Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.haalrubaye.meteorite.Addon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.function.Predicate;

public class BlockIn extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("What blocks to use for surround.")
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay, in ticks, between block placements.")
        .min(0)
        .defaultValue(0)
        .build()
    );

    private final Setting<meteordevelopment.meteorclient.systems.modules.combat.Surround.Center> center = sgGeneral.add(new EnumSetting.Builder<meteordevelopment.meteorclient.systems.modules.combat.Surround.Center>()
        .name("center")
        .description("Teleports you to the center of the block.")
        .defaultValue(meteordevelopment.meteorclient.systems.modules.combat.Surround.Center.Incomplete)
        .build()
    );

    private final Setting<Boolean> doubleHeight = sgGeneral.add(new BoolSetting.Builder()
        .name("double-height")
        .description("Places obsidian on top of the original surround blocks to prevent people from face-placing you.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Works only when you are standing on blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleOnYChange = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-y-change")
        .description("Automatically disables when your y level changes (step, jumping, etc).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleOnComplete = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-complete")
        .description("Toggles off when all blocks are placed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically faces towards the obsidian being placed.")
        .defaultValue(true)
        .build()
    );

    private final BlockPos.Mutable placePos = new BlockPos.Mutable();
    private final BlockPos.Mutable testPos = new BlockPos.Mutable();
    private int ticks;

    public BlockIn() {
        super(Addon.CATEGORY, "Block In", "Surrounds you in blocks.");
    }


    @Override
    public void onActivate() {
        // Center on activate
        if (center.get() == meteordevelopment.meteorclient.systems.modules.combat.Surround.Center.OnActivate) PlayerUtils.centerPlayer();

        // Reset delay
        ticks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Toggle if Y level changed
        if (toggleOnYChange.get() && mc.player.prevY < mc.player.getY()) {
            toggle();
            return;
        }

        // Wait till player is on ground
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;

        // Wait until the player has a block available to place
        if (!getInvBlock().found()) return;

        // Centering player
        if (center.get() == meteordevelopment.meteorclient.systems.modules.combat.Surround.Center.Always) PlayerUtils.centerPlayer();

        // Tick the placement timer
        if (ticks > 0) {
            ticks--;
            return;
        }
        else {
            ticks = delay.get();
        }

        // Check surround blocks in order and place the first missing one if present
        int safe = 0;

        // Looping through feet blocks
        for (CardinalDirection direction : CardinalDirection.values()) {
            if (place(direction, 0)) break;
            safe++;
        }

        // Looping through head blocks
        if (doubleHeight.get() && safe == 4) {
            for (CardinalDirection direction : CardinalDirection.values()) {
                if (place(direction, 1)) break;
                safe++;
            }
        }

        boolean complete = safe == (doubleHeight.get() ? 8 : 4);

        // Disable if all the surround blocks are placed
        if (complete && toggleOnComplete.get()) {
            toggle();
            return;
        }

        // Keep the player centered until all the blocks are placed to avoid collision
        if (!complete && center.get() == meteordevelopment.meteorclient.systems.modules.combat.Surround.Center.Incomplete) PlayerUtils.centerPlayer();
    }

    private boolean place(CardinalDirection direction, int y) {
        placePos.set(offsetPosFromPlayer(direction, y));

        if (mc.world.getBlockState(offsetPos(placePos, null, -1)).isAir()) {
            return false;
        }

        // Attempt to place
        boolean placed = BlockUtils.place(
            placePos,
            getInvBlock(),
            rotate.get(),
            100,
            true
        );

        return placed;
    }

    private BlockPos.Mutable offsetPosFromPlayer(CardinalDirection direction, int y) {
        return offsetPos(mc.player.getBlockPos(), direction, y);
    }

    private BlockPos.Mutable offsetPos(BlockPos origin, CardinalDirection direction, int y) {
        if (direction == null) {
            return testPos.set(
                origin.getX(),
                origin.getY() + y,
                origin.getZ()
            );
        }

        return testPos.set(
            origin.getX() + direction.toDirection().getOffsetX(),
            origin.getY() + y,
            origin.getZ() + direction.toDirection().getOffsetZ()
        );
    }

    private BlockPos.Mutable offsetPos(BlockPos.Mutable origin, CardinalDirection direction, int y) {
        if (direction == null) {
            return testPos.set(
                origin.getX(),
                origin.getY() + y,
                origin.getZ()
            );
        }

        return testPos.set(
            origin.getX() + direction.toDirection().getOffsetX(),
            origin.getY() + y,
            origin.getZ() + direction.toDirection().getOffsetZ()
        );
    }

    private FindItemResult getInvBlock() {
        return InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
    }
}
