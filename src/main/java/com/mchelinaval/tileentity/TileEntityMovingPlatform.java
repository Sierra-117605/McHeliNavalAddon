package com.mchelinaval.tileentity;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.util.McheliReflect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移動プラットフォームのTileEntity。
 *
 * 【モード】
 *   ELEVATOR  : Y軸方向（上下）に移動 → デッキエレベーター
 *   JBD       : Y+Z同時（斜め）に移動 → ジェットブラストデフレクター
 *
 * 【使い方】
 *   1. コントローラーブロックを構造物に設置
 *   2. スニーク右クリックでモード選択
 *   3. 右クリックで移動開始/停止
 *
 * 【JBDモード】
 *   カタパルトに隣接していれば自動でカタパルトと連動する。
 *   手動でも右クリックで動かせる。
 */
public class TileEntityMovingPlatform extends TileEntity implements ITickable {

    // -------------------------------------------------------
    // 移動モード
    // -------------------------------------------------------
    public enum Mode {
        ELEVATOR,   // Y軸（エレベーター）
        JBD         // Y+Z同時・斜め（ジェットブラストデフレクター）
    }

    // -------------------------------------------------------
    // 状態
    // -------------------------------------------------------
    private Mode mode = Mode.ELEVATOR;

    // 移動距離（ブロック数）
    private int travelDistance = 5;

    // 移動速度（毎tick何ブロック移動するか。0.05 = 1秒で1ブロック程度）
    private double moveSpeed = 0.05;

    // 対象範囲（コントローラーから何ブロック以内のブロックを動かすか）
    private int range = 5;

    // カタパルト連動モードか
    private boolean catapultLinked = false;

    // 現在の移動状態
    private boolean moving = false;
    private boolean deploying = true;   // true=展開方向, false=格納方向

    // 移動済み距離（ブロック単位で管理）
    private double movedDistance = 0.0;

    // 登録済みの対象ブロック（コントローラー基準の相対座標）
    private List<BlockPos> targetOffsets = new ArrayList<>();

    // 対象ブロックの実際の移動量（ワールド座標で管理）
    private Map<BlockPos, double[]> blockOffsets = new HashMap<>();

    // -------------------------------------------------------
    // ITickable: 毎tick呼ばれる
    // -------------------------------------------------------
    @Override
    public void update() {
        if (world == null || world.isRemote || !moving) return;

        // 移動限界チェック
        if (movedDistance >= travelDistance) {
            if (!catapultLinked) {
                // 手動モードは端に着いたら自動で折り返す
                deploying = !deploying;
                movedDistance = 0.0;
                moving = false; // 一旦停止（再度右クリックで動かす）
            }
            return;
        }

        // 移動量を計算
        double step = Math.min(moveSpeed, travelDistance - movedDistance);
        movedDistance += step;

        // モードに応じた移動方向ベクトル
        double dy = 0, dz = 0;
        if (mode == Mode.ELEVATOR) {
            dy = deploying ? step : -step;
        } else if (mode == Mode.JBD) {
            // 斜め45°：Y方向とZ方向に同量移動
            dy = deploying ? step : -step;
            dz = deploying ? -step : step; // 前方向へ展開
        }

        // 対象ブロック上のエンティティを移動させる
        moveEntitiesOnBlocks(dy, dz);
    }

    /**
     * 対象ブロック上にいるエンティティ（機体・プレイヤー）を動かす。
     * ブロック自体はMinecraftの制約上動かせないため、
     * エンティティのみ移動させる仕組み。
     *
     * ※ブロック自体を動かす機能はValkyrien Skiesなど別MODの領域。
     *   本MODではエンティティの移動に特化する。
     */
    private void moveEntitiesOnBlocks(double dy, double dz) {
        if (world == null) return;

        // コントローラーブロック周囲の範囲でエンティティを検索
        AxisAlignedBB searchBox = new AxisAlignedBB(
            pos.getX() - range, pos.getY() - 1,     pos.getZ() - range,
            pos.getX() + range, pos.getY() + range + 3, pos.getZ() + range
        );

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, searchBox);
        for (Entity entity : entities) {
            // プレイヤーまたはMCHELI機体を対象とする
            if (entity instanceof net.minecraft.entity.player.EntityPlayer
                    || McheliReflect.isMcheliAircraft(entity)) {
                entity.setPosition(
                    entity.posX,
                    entity.posY + dy,
                    entity.posZ + dz
                );
            }
        }
    }

    // -------------------------------------------------------
    // 外部からの操作メソッド
    // -------------------------------------------------------

    /** 右クリック：移動開始/停止トグル */
    public void toggle() {
        moving = !moving;
        if (moving) {
            McHeliNavalAddon.logger.info("MovingPlatform toggled ON at {}", pos);
        }
    }

    /** カタパルトから「機体セット」通知 → 展開開始 */
    public void deploy() {
        if (!moving || !deploying) {
            deploying = true;
            movedDistance = 0.0;
            moving = true;
            McHeliNavalAddon.logger.info("MovingPlatform deploy (JBD) at {}", pos);
        }
    }

    /** カタパルトから「機体離脱」通知 → 格納開始 */
    public void retract() {
        deploying = false;
        movedDistance = 0.0;
        moving = true;
        McHeliNavalAddon.logger.info("MovingPlatform retract (JBD) at {}", pos);
    }

    /** スニーク右クリック：モードをサイクル */
    public void cycleMode() {
        Mode[] modes = Mode.values();
        int next = (mode.ordinal() + 1) % modes.length;
        mode = modes[next];
        movedDistance = 0.0;
        moving = false;
    }

    public String getModeDescription() {
        switch (mode) {
            case ELEVATOR: return "ELEVATOR (Y axis) - dist:" + travelDistance + "blk";
            case JBD:      return "JBD (Y+Z diagonal) - dist:" + travelDistance + "blk";
            default:       return mode.name();
        }
    }

    public boolean isCatapultLinked() {
        return catapultLinked || mode == Mode.JBD;
    }

    public void setCatapultLinked(boolean linked) {
        this.catapultLinked = linked;
    }

    // -------------------------------------------------------
    // NBT保存・読み込み（ワールドを閉じても設定が消えないように）
    // -------------------------------------------------------
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("mode", mode.ordinal());
        compound.setInteger("travelDistance", travelDistance);
        compound.setDouble("moveSpeed", moveSpeed);
        compound.setInteger("range", range);
        compound.setBoolean("catapultLinked", catapultLinked);
        compound.setDouble("movedDistance", movedDistance);
        compound.setBoolean("moving", moving);
        compound.setBoolean("deploying", deploying);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        Mode[] modes = Mode.values();
        int modeIdx = compound.getInteger("mode");
        mode = (modeIdx >= 0 && modeIdx < modes.length) ? modes[modeIdx] : Mode.ELEVATOR;
        travelDistance = compound.getInteger("travelDistance");
        if (travelDistance <= 0) travelDistance = 5; // デフォルト
        moveSpeed = compound.getDouble("moveSpeed");
        if (moveSpeed <= 0) moveSpeed = 0.05;
        range = compound.getInteger("range");
        if (range <= 0) range = 5;
        catapultLinked = compound.getBoolean("catapultLinked");
        movedDistance = compound.getDouble("movedDistance");
        moving = compound.getBoolean("moving");
        deploying = compound.getBoolean("deploying");
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
