package com.mchelinaval.tileentity;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.block.BlockFloorMarker;
import com.mchelinaval.registry.ModBlocks;
import com.mchelinaval.util.McheliReflect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 移動プラットフォームのTileEntity。
 *
 * 【モード】
 *   ELEVATOR : Y軸方向。フロアマーカーで止まる位置を決める。複数フロア対応
 *   JBD      : Y+Z同時（斜め）。カタパルト連動。固定距離で動く
 *
 * 【エレベーターの使い方】
 *   1. コントローラーブロックをエレベーターシャフトに設置
 *   2. 止まりたい各フロアの床にフロアマーカーブロックを設置
 *   3. 右クリックで「次のフロアへ」移動
 *   4. スニーク+右クリックで「前のフロアへ」（下方向）
 *
 * 【JBDの使い方】
 *   カタパルトに隣接させて設置するだけで自動連動
 */
public class TileEntityMovingPlatform extends TileEntity implements ITickable {

    // configから変更可能なデフォルト値
    public static double DEFAULT_MOVE_SPEED      = 0.05;
    public static int    DEFAULT_TRAVEL_DISTANCE = 5;
    public static int    DEFAULT_RANGE           = 5;

    // フロアマーカーを探す最大範囲（上下それぞれ）
    private static final int FLOOR_SEARCH_RANGE = 32;

    public enum Mode { ELEVATOR, JBD }

    // -------------------------------------------------------
    // 状態
    // -------------------------------------------------------
    private Mode    mode        = Mode.ELEVATOR;
    private double  moveSpeed   = DEFAULT_MOVE_SPEED;
    private int     range       = DEFAULT_RANGE;
    private boolean moving      = false;
    private boolean goingUp     = true;

    // 現在のエンティティY座標オフセット（実際に動かした累積量）
    private double currentOffset = 0.0;

    // 目標Y座標（フロアマーカーのY + 1 をエンティティの目標とする）
    private double targetY = Double.NaN;

    // JBD用: 固定距離・斜め移動
    private int    travelDistance = DEFAULT_TRAVEL_DISTANCE;
    private double jbdMoved       = 0.0;
    private boolean jbdDeploying  = true;
    private boolean catapultLinked = false;

    // -------------------------------------------------------
    // ITickable
    // -------------------------------------------------------
    @Override
    public void update() {
        if (world == null || world.isRemote || !moving) return;

        if (mode == Mode.ELEVATOR) {
            tickElevator();
        } else {
            tickJBD();
        }
    }

    // -------------------------------------------------------
    // エレベーター毎tick処理
    // -------------------------------------------------------
    private void tickElevator() {
        if (Double.isNaN(targetY)) {
            moving = false;
            return;
        }

        // 現在のエンティティ代表Y（コントローラーY + オフセット）
        double currentY = pos.getY() + currentOffset;
        double remaining = targetY - currentY;

        if (Math.abs(remaining) <= moveSpeed) {
            // 目標に到達 → 停止
            moveEntities(remaining, 0);
            currentOffset += remaining;
            moving = false;
            targetY = Double.NaN;
            McHeliNavalAddon.logger.info("Elevator arrived at Y={}", (int)(pos.getY() + currentOffset));
        } else {
            // 移動中
            double step = moveSpeed * (goingUp ? 1 : -1);
            moveEntities(step, 0);
            currentOffset += step;
        }
    }

    // -------------------------------------------------------
    // JBD毎tick処理
    // -------------------------------------------------------
    private void tickJBD() {
        if (jbdMoved >= travelDistance) {
            moving = false;
            return;
        }
        double step = Math.min(moveSpeed, travelDistance - jbdMoved);
        jbdMoved += step;

        double dy = jbdDeploying ?  step : -step;
        double dz = jbdDeploying ? -step :  step;
        moveEntities(dy, dz);
    }

    // -------------------------------------------------------
    // 操作メソッド
    // -------------------------------------------------------

    /** 右クリック：次のフロアへ（上） */
    public void goUp(EntityPlayer player) {
        if (mode != Mode.ELEVATOR) { toggle(); return; }
        List<Integer> floors = findFloorYs();
        double currentY = pos.getY() + currentOffset;

        // 現在より高いフロアのうち最小のもの
        Integer nextFloor = null;
        for (int fy : floors) {
            double floorEntityY = fy + 1.0; // マーカーの1ブロック上がエンティティの立ち位置
            if (floorEntityY > currentY + 0.5) {
                nextFloor = fy;
                break;
            }
        }

        if (nextFloor == null) {
            if (player != null) player.sendMessage(new TextComponentString("[Naval] 上にフロアマーカーがありません"));
            return;
        }

        targetY = nextFloor + 1.0;
        goingUp = true;
        moving  = true;
        markDirty();
        syncToClient();
        McHeliNavalAddon.logger.info("Elevator going UP to floor Y={}", nextFloor);
    }

    /** スニーク+右クリック：前のフロアへ（下） */
    public void goDown(EntityPlayer player) {
        if (mode != Mode.ELEVATOR) { toggle(); return; }
        List<Integer> floors = findFloorYs();
        double currentY = pos.getY() + currentOffset;

        // 現在より低いフロアのうち最大のもの
        Integer nextFloor = null;
        for (int i = floors.size() - 1; i >= 0; i--) {
            double floorEntityY = floors.get(i) + 1.0;
            if (floorEntityY < currentY - 0.5) {
                nextFloor = floors.get(i);
                break;
            }
        }

        if (nextFloor == null) {
            if (player != null) player.sendMessage(new TextComponentString("[Naval] 下にフロアマーカーがありません"));
            return;
        }

        targetY = nextFloor + 1.0;
        goingUp = false;
        moving  = true;
        markDirty();
        syncToClient();
        McHeliNavalAddon.logger.info("Elevator going DOWN to floor Y={}", nextFloor);
    }

    /** JBD展開（カタパルトから通知） */
    public void deploy() {
        if (mode != Mode.JBD || moving) return;
        jbdDeploying = true;
        jbdMoved = 0.0;
        moving = true;
    }

    /** JBD格納（カタパルトから通知） */
    public void retract() {
        if (mode != Mode.JBD) return;
        jbdDeploying = false;
        jbdMoved = 0.0;
        moving = true;
    }

    /** JBD手動トグル */
    public void toggle() {
        moving = !moving;
    }

    /** モードサイクル（スニーク+右クリック in JBDモード / またはELEVATORモードで下へ） */
    public void cycleMode() {
        Mode[] modes = Mode.values();
        mode = modes[(mode.ordinal() + 1) % modes.length];
        moving = false;
        currentOffset = 0.0;
        targetY = Double.NaN;
        jbdMoved = 0.0;
        markDirty();
        syncToClient();
    }

    public String getModeDescription() {
        if (mode == Mode.ELEVATOR) {
            List<Integer> floors = findFloorYs();
            return "ELEVATOR - " + floors.size() + "フロア検知 | 右クリック=上 スニーク+右クリック=下";
        }
        return "JBD (斜め展開) | 右クリック=手動 / カタパルト隣接で自動連動";
    }

    public Mode getMode() { return mode; }
    public boolean isMoving() { return moving; }
    /** 現在エンティティがいるY座標（コントローラーY＋移動オフセット） */
    public double getCurrentY() { return pos.getY() + currentOffset; }
    public boolean isCatapultLinked() { return catapultLinked || mode == Mode.JBD; }
    public void setCatapultLinked(boolean v) { catapultLinked = v; }

    // -------------------------------------------------------
    // フロアマーカーを探してYリストを返す（昇順ソート済み）
    // -------------------------------------------------------
    private List<Integer> findFloorYs() {
        List<Integer> result = new ArrayList<>();
        if (world == null) return result;

        int cx = pos.getX();
        int cz = pos.getZ();
        int baseY = pos.getY();

        // コントローラーの真上・真下 FLOOR_SEARCH_RANGE ブロックを走査
        for (int dy = -FLOOR_SEARCH_RANGE; dy <= FLOOR_SEARCH_RANGE; dy++) {
            int checkY = baseY + dy;
            if (checkY < 0 || checkY > 255) continue;
            BlockPos checkPos = new BlockPos(cx, checkY, cz);
            IBlockState state = world.getBlockState(checkPos);
            if (state.getBlock() instanceof BlockFloorMarker) {
                result.add(checkY);
            }
        }

        Collections.sort(result);
        return result;
    }

    // -------------------------------------------------------
    // エンティティ移動
    // -------------------------------------------------------
    private void moveEntities(double dy, double dz) {
        if (world == null) return;

        AxisAlignedBB box = new AxisAlignedBB(
            pos.getX() - range, pos.getY() + currentOffset - 1,     pos.getZ() - range,
            pos.getX() + range, pos.getY() + currentOffset + range + 3, pos.getZ() + range
        );

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, box);
        for (Entity e : entities) {
            if (e instanceof EntityPlayer || McheliReflect.isMcheliAircraft(e)) {
                e.setPosition(e.posX, e.posY + dy, e.posZ + dz);
            }
        }
    }

    // -------------------------------------------------------
    // NBT保存・読み込み
    // -------------------------------------------------------
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("mode", mode.ordinal());
        tag.setDouble("moveSpeed", moveSpeed);
        tag.setInteger("range", range);
        tag.setInteger("travelDistance", travelDistance);
        tag.setBoolean("moving", moving);
        tag.setBoolean("goingUp", goingUp);
        tag.setDouble("currentOffset", currentOffset);
        tag.setBoolean("catapultLinked", catapultLinked);
        tag.setDouble("jbdMoved", jbdMoved);
        tag.setBoolean("jbdDeploying", jbdDeploying);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        Mode[] modes = Mode.values();
        int mi = tag.getInteger("mode");
        mode          = (mi >= 0 && mi < modes.length) ? modes[mi] : Mode.ELEVATOR;
        moveSpeed     = tag.hasKey("moveSpeed")     ? tag.getDouble("moveSpeed")   : DEFAULT_MOVE_SPEED;
        range         = tag.hasKey("range")         ? tag.getInteger("range")      : DEFAULT_RANGE;
        travelDistance= tag.hasKey("travelDistance")? tag.getInteger("travelDistance"): DEFAULT_TRAVEL_DISTANCE;
        moving        = tag.getBoolean("moving");
        goingUp       = tag.getBoolean("goingUp");
        currentOffset = tag.getDouble("currentOffset");
        catapultLinked= tag.getBoolean("catapultLinked");
        jbdMoved      = tag.getDouble("jbdMoved");
        jbdDeploying  = tag.getBoolean("jbdDeploying");
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return old.getBlock() != nw.getBlock();
    }

    // -------------------------------------------------------
    // クライアントへの状態同期（GUIの表示に使う）
    // -------------------------------------------------------

    /** サーバー→クライアントへTileEntityデータを送るパケットを生成 */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    /** 同期するNBTデータ（全フィールドを含む） */
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    /** クライアント側でパケットを受け取ったときにNBTを反映 */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    /** ブロック更新通知でクライアントへ同期する */
    private void syncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }
}
