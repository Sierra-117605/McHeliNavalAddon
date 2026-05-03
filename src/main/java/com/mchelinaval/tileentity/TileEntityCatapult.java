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
 * 【JBD自動連動】
 *   カタパルトに隣接（6面）するJBDコントローラーを自動検出する。
 *   - 機体が乗った瞬間 → 隣接JBDをdeploy（展開）
 *   - 機体が離れた瞬間 → 隣接JBDをretract（格納）
 *   ※ リンク登録不要。隣に置くだけで連動する。
 */
public class TileEntityCatapult extends TileEntity implements ITickable {

    public static float LAUNCH_SPEED = 3.0f;

    /** 前tickの艦載機検知状態（変化を検出するために保持） */
    private boolean wasAircraftPresent = false;
    /** tickカウンター（10tickごとにチェック） */
    private int tickCounter = 0;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    /**
     * 毎tick処理。
     * 10tickごとに艦載機の有無をチェックし、変化があれば隣接JBDに通知する。
     */
    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        // 10tickに1回だけチェック（パフォーマンス節約）
        if (++tickCounter < 10) return;
        tickCounter = 0;

        boolean aircraftNow = getAircraftOnBlock() != null;
        // 前tickと状態が変わったときだけ通知
        if (aircraftNow != wasAircraftPresent) {
            wasAircraftPresent = aircraftNow;
            notifyAdjacentJBDs(aircraftNow);
        }
    }

    /**
     * カタパルトに隣接する（上下左右前後）JBDコントローラーを探し、
     * deploy=trueなら展開、falseなら格納を呼ぶ。
     */
    private void notifyAdjacentJBDs(boolean deploy) {
        for (EnumFacing face : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te instanceof TileEntityJBDController) {
                if (deploy) {
                    ((TileEntityJBDController) te).deploy();
                } else {
                    ((TileEntityJBDController) te).retract();
                }
                McHeliNavalAddon.logger.info("Catapult notified JBD at {} → {}",
                    pos.offset(face), deploy ? "deploy" : "retract");
            }
        }
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
