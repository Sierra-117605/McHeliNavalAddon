package com.mchelinaval.tileentity;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.block.BlockFloorMarker;
import com.mchelinaval.util.McheliReflect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * エレベーターコントローラーのTileEntity。
 *
 * 【仕組み】
 *   - コントローラーブロックを1つ設置する
 *   - 止まりたい各フロアの床にフロアマーカーを敷き詰める（囲むブロック）
 *   - フロアマーカーが乗り場の床になり、コントローラーが止まるY座標を決める
 *
 * 【移動対象】
 *   - コントローラー周辺のプレイヤー
 *   - コントローラー周辺のMCHELI機体
 *
 * テクスチャ偽装：左クリックでブロックのテクスチャに変更できる
 */
public class TileEntityElevatorController extends TileEntity implements ITickable, IHasMimic {

    // configから変更可能なデフォルト値
    public static double DEFAULT_MOVE_SPEED = 0.05;
    public static int    DEFAULT_RANGE      = 5;

    // フロアマーカーを探す最大範囲（上下それぞれ）
    private static final int FLOOR_SEARCH_RANGE = 32;

    // -------------------------------------------------------
    // 状態
    // -------------------------------------------------------
    private double  moveSpeed     = DEFAULT_MOVE_SPEED;
    private int     range         = DEFAULT_RANGE;
    private boolean moving        = false;
    private boolean goingUp       = true;

    /** 現在エンティティがいるY座標オフセット（コントローラーY基準） */
    private double  currentOffset = 0.0;

    /** 目標Y座標（フロアマーカーのY + 1） */
    private double  targetY       = Double.NaN;

    /** テクスチャ偽装ブロック */
    private IBlockState mimicState = null;

    // -------------------------------------------------------
    // ITickable
    // -------------------------------------------------------
    @Override
    public void update() {
        if (world == null || world.isRemote || !moving) return;
        tickElevator();
    }

    // -------------------------------------------------------
    // エレベーター毎tick処理
    // -------------------------------------------------------
    private void tickElevator() {
        if (Double.isNaN(targetY)) { moving = false; return; }

        double currentY   = pos.getY() + currentOffset;
        double remaining  = targetY - currentY;

        if (Math.abs(remaining) <= moveSpeed) {
            // 目標到達 → ぴったり合わせて停止
            moveEntities(remaining, 0);
            currentOffset += remaining;
            moving  = false;
            targetY = Double.NaN;
            syncToClient();
            McHeliNavalAddon.logger.info("Elevator arrived at Y={}", (int)(pos.getY() + currentOffset));
        } else {
            double step = moveSpeed * (goingUp ? 1 : -1);
            moveEntities(step, 0);
            currentOffset += step;
        }
    }

    // -------------------------------------------------------
    // 操作メソッド
    // -------------------------------------------------------

    /** 上のフロアへ移動 */
    public void goUp(EntityPlayer player) {
        List<Integer> floors = findFloorYs();
        double currentY = pos.getY() + currentOffset;

        Integer nextFloor = null;
        for (int fy : floors) {
            if ((fy + 1.0) > currentY + 0.5) { nextFloor = fy; break; }
        }

        if (nextFloor == null) {
            if (player != null)
                player.sendMessage(new TextComponentString("[Naval] 上にフロアマーカーがありません"));
            return;
        }

        targetY = nextFloor + 1.0;
        goingUp = true;
        moving  = true;
        markDirty(); syncToClient();
        McHeliNavalAddon.logger.info("Elevator going UP to Y={}", nextFloor);
    }

    /** 下のフロアへ移動 */
    public void goDown(EntityPlayer player) {
        List<Integer> floors = findFloorYs();
        double currentY = pos.getY() + currentOffset;

        Integer nextFloor = null;
        for (int i = floors.size() - 1; i >= 0; i--) {
            if ((floors.get(i) + 1.0) < currentY - 0.5) { nextFloor = floors.get(i); break; }
        }

        if (nextFloor == null) {
            if (player != null)
                player.sendMessage(new TextComponentString("[Naval] 下にフロアマーカーがありません"));
            return;
        }

        targetY = nextFloor + 1.0;
        goingUp = false;
        moving  = true;
        markDirty(); syncToClient();
        McHeliNavalAddon.logger.info("Elevator going DOWN to Y={}", nextFloor);
    }

    // -------------------------------------------------------
    // フロアマーカーをY高さで探す（昇順ソート）
    // -------------------------------------------------------
    /**
     * コントローラー周辺 range×range の範囲内にあるフロアマーカーの
     * Y座標一覧を返す（重複なし・昇順）。
     *
     * 【以前の設計】コントローラーと同じ X/Z 列のみ検索 → 使いにくかった
     * 【現在の設計】range 内にあるどのフロアマーカーも停止位置として認識する
     */
    private List<Integer> findFloorYs() {
        Set<Integer> foundYs = new HashSet<>();
        if (world == null) return new ArrayList<>();

        int cx = pos.getX(), cz = pos.getZ(), base = pos.getY();

        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                for (int dy = -FLOOR_SEARCH_RANGE; dy <= FLOOR_SEARCH_RANGE; dy++) {
                    int y = base + dy;
                    if (y < 0 || y > 255) continue;
                    if (world.getBlockState(new BlockPos(cx + dx, y, cz + dz))
                             .getBlock() instanceof BlockFloorMarker) {
                        foundYs.add(y);
                    }
                }
            }
        }

        List<Integer> result = new ArrayList<>(foundYs);
        Collections.sort(result);
        McHeliNavalAddon.logger.info("[ElevatorTE] findFloorYs: {} フロアを発見 → {}",
            result.size(), result);
        return result;
    }

    // -------------------------------------------------------
    // エンティティ移動（プレイヤー + MCHELI機体）
    // -------------------------------------------------------
    private void moveEntities(double dy, double dz) {
        if (world == null) return;
        AxisAlignedBB box = new AxisAlignedBB(
            pos.getX() - range, pos.getY() + currentOffset - 1,         pos.getZ() - range,
            pos.getX() + range, pos.getY() + currentOffset + range + 3, pos.getZ() + range
        );
        for (Entity e : world.getEntitiesWithinAABB(Entity.class, box)) {
            if (e instanceof EntityPlayer || McheliReflect.isMcheliAircraft(e)) {
                double nx = e.posX;
                double ny = e.posY + dy;
                double nz = e.posZ + dz;

                if (e instanceof EntityPlayerMP) {
                    // プレイヤーは setPosition だけではクライアント側で無視される。
                    // setPlayerLocation でテレポートパケットを送って強制移動させる。
                    ((EntityPlayerMP) e).connection.setPlayerLocation(nx, ny, nz,
                        e.rotationYaw, e.rotationPitch);
                } else {
                    e.setPosition(nx, ny, nz);
                }
            }
        }
    }

    // -------------------------------------------------------
    // ゲッター
    // -------------------------------------------------------
    public boolean isMoving()    { return moving; }
    public double getCurrentY()  { return pos.getY() + currentOffset; }

    // -------------------------------------------------------
    // IHasMimic
    // -------------------------------------------------------
    @Override public IBlockState getMimicState() { return mimicState; }
    @Override public void setMimicState(IBlockState state) {
        this.mimicState = state;
        markDirty(); syncToClient();
    }

    // -------------------------------------------------------
    // NBT
    // -------------------------------------------------------
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("moveSpeed",     moveSpeed);
        tag.setInteger("range",        range);
        tag.setBoolean("moving",       moving);
        tag.setBoolean("goingUp",      goingUp);
        tag.setDouble("currentOffset", currentOffset);
        if (!Double.isNaN(targetY)) tag.setDouble("targetY", targetY);
        TileEntityFloorMarker.writeMimicToTag(tag, mimicState);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        moveSpeed     = tag.hasKey("moveSpeed")     ? tag.getDouble("moveSpeed")   : DEFAULT_MOVE_SPEED;
        range         = tag.hasKey("range")         ? tag.getInteger("range")      : DEFAULT_RANGE;
        moving        = tag.getBoolean("moving");
        goingUp       = tag.getBoolean("goingUp");
        currentOffset = tag.getDouble("currentOffset");
        targetY       = tag.hasKey("targetY")       ? tag.getDouble("targetY")     : Double.NaN;
        mimicState    = TileEntityFloorMarker.readMimicFromTag(tag);
    }

    // -------------------------------------------------------
    // クライアント同期
    // -------------------------------------------------------
    @Override public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }
    @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
    @Override public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
    private void syncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
        }
    }
    @Override public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return old.getBlock() != nw.getBlock();
    }
}
