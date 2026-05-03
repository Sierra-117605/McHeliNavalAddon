package com.mchelinaval.tileentity;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.block.BlockCatapult;
import com.mchelinaval.util.McheliReflect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * カタパルトのTileEntity。
 * fire() が呼ばれると、ブロック上のMCHELI機体を前方へ打ち出す。
 *
 * JBDとの連動は廃止。JBDは独立したJBDコントローラーで手動操作する。
 */
public class TileEntityCatapult extends TileEntity implements ITickable {

    public static float LAUNCH_SPEED = 3.0f;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    /** 毎tick処理（現在は特に何もしない。JBD連動は廃止済み） */
    @Override
    public void update() {
        // JBD自動連動を廃止。JBDは独立してGUIから手動操作する。
    }

    // -------------------------------------------------------
    // カタパルト発射処理
    // -------------------------------------------------------
    public void fire() {
        if (world == null || world.isRemote) return;

        Entity aircraft = getAircraftOnBlock();
        if (aircraft == null) {
            McHeliNavalAddon.logger.info("Catapult: no aircraft on block at {}", pos);
            return;
        }

        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(BlockCatapult.FACING);

        double vx = facing.getXOffset() * LAUNCH_SPEED;
        double vz = facing.getZOffset() * LAUNCH_SPEED;
        double vy = 0.3;

        McheliReflect.setVelocityX(aircraft, vx);
        McheliReflect.setVelocityY(aircraft, vy);
        McheliReflect.setVelocityZ(aircraft, vz);

        spawnSteamParticles(facing);
        world.playSound(null, pos,
            SoundEvents.BLOCK_PISTON_EXTEND,
            SoundCategory.BLOCKS, 1.5f, 0.6f);

        McHeliNavalAddon.logger.info("Catapult fired: vx={}, vy={}, vz={}", vx, vy, vz);
    }

    private Entity getAircraftOnBlock() {
        if (world == null) return null;
        AxisAlignedBB box = new AxisAlignedBB(
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1
        );
        for (Entity e : world.getEntitiesWithinAABB(Entity.class, box)) {
            if (McheliReflect.isMcheliAircraft(e)) return e;
        }
        return null;
    }

    private void spawnSteamParticles(EnumFacing facing) {
        if (world == null) return;
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;
        double cz = pos.getZ() + 0.5;
        for (int i = 0; i < 20; i++) {
            double ox = (world.rand.nextDouble() - 0.5) * 1.5;
            double oy = world.rand.nextDouble() * 1.5;
            double oz = (world.rand.nextDouble() - 0.5) * 1.5;
            world.spawnParticle(EnumParticleTypes.CLOUD, cx + ox, cy, cz + oz, ox * 0.1, oy * 0.1, oz * 0.1);
        }
    }
}
