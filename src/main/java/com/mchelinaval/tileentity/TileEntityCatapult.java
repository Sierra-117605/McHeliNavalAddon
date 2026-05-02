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
 * また、隣接する移動プラットフォーム（TileEntityMovingPlatform）に
 * 機体のセット状態を通知してJBDを連動させる。
 */
public class TileEntityCatapult extends TileEntity implements ITickable {

    // 打ち出し速度（デフォルト値。将来的にconfigで変更可）
    public static float LAUNCH_SPEED = 3.0f;

    // 機体がブロック上にいるかの監視用（JBD連動のため毎tickチェック）
    private boolean wasOccupied = false;

    // -------------------------------------------------------
    // 毎tick処理（機体の乗り降りを検知してJBDに通知）
    // -------------------------------------------------------
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void update() {
        if (world == null || world.isRemote) return;

        boolean isOccupied = getAircraftOnBlock() != null;

        if (isOccupied && !wasOccupied) {
            // 機体が乗った → JBDに「展開」通知
            notifyJBD(true);
        } else if (!isOccupied && wasOccupied) {
            // 機体が離れた → JBDに「格納」通知
            notifyJBD(false);
        }

        wasOccupied = isOccupied;
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

        // ブロックの向きを取得して打ち出し方向を決める
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(BlockCatapult.FACING);

        double vx = facing.getXOffset() * LAUNCH_SPEED;
        double vz = facing.getZOffset() * LAUNCH_SPEED;
        double vy = 0.3; // わずかに上向きに打ち出す

        // MCHELIの速度フィールドをリフレクションで書き換える
        McheliReflect.setVelocityX(aircraft, vx);
        McheliReflect.setVelocityY(aircraft, vy);
        McheliReflect.setVelocityZ(aircraft, vz);

        // スチームエフェクト
        spawnSteamParticles(facing);

        // 効果音（ピストン音で代用）
        world.playSound(null, pos,
            SoundEvents.BLOCK_PISTON_EXTEND,
            SoundCategory.BLOCKS, 1.5f, 0.6f);

        McHeliNavalAddon.logger.info("Catapult fired: vx={}, vy={}, vz={}", vx, vy, vz);
    }

    // -------------------------------------------------------
    // ブロック上のMCHELI機体を取得する
    // -------------------------------------------------------
    private Entity getAircraftOnBlock() {
        if (world == null) return null;

        // ブロックの1ブロック上の範囲でエンティティを検索
        AxisAlignedBB searchBox = new AxisAlignedBB(
            pos.getX(), pos.getY(),     pos.getZ(),
            pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1
        );

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, searchBox);
        for (Entity entity : entities) {
            if (McheliReflect.isMcheliAircraft(entity)) {
                return entity;
            }
        }
        return null;
    }

    // -------------------------------------------------------
    // 隣接するTileEntityMovingPlatformにJBD展開/格納を通知
    // -------------------------------------------------------
    private void notifyJBD(boolean deploy) {
        if (world == null) return;

        // 隣接する全方向をチェック
        for (EnumFacing dir : EnumFacing.values()) {
            BlockPos neighborPos = pos.offset(dir);
            TileEntity te = world.getTileEntity(neighborPos);
            if (te instanceof TileEntityMovingPlatform) {
                TileEntityMovingPlatform platform = (TileEntityMovingPlatform) te;
                if (platform.isCatapultLinked()) {
                    if (deploy) {
                        platform.deploy();
                    } else {
                        platform.retract();
                    }
                }
            }
        }
    }

    // -------------------------------------------------------
    // スチームパーティクルを出す
    // -------------------------------------------------------
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
