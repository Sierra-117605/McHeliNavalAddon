package com.mchelinaval.tileentity;

import com.mchelinaval.McHeliNavalAddon;
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
import net.minecraft.world.World;

import java.util.List;

/**
 * JBD（ジェットブラストデフレクター）コントローラーのTileEntity。
 *
 * 【仕組み】
 *   - JBDコントローラーを1つ設置する
 *   - 周りにフロアマーカーを敷いてデフレクター面を構成する（囲むブロック）
 *   - GUIの「展開」ボタンでY+Z方向に45°斜め移動
 *   - カタパルトとは独立して運用する（自動連動なし）
 *
 * テクスチャ偽装：左クリックでブロックのテクスチャに変更できる
 */
public class TileEntityJBDController extends TileEntity implements ITickable, IHasMimic {

    // configから変更可能なデフォルト値
    public static double DEFAULT_MOVE_SPEED      = 0.05;
    public static int    DEFAULT_TRAVEL_DISTANCE = 5;
    public static int    DEFAULT_RANGE           = 5;

    // -------------------------------------------------------
    // 状態
    // -------------------------------------------------------
    private double  moveSpeed      = DEFAULT_MOVE_SPEED;
    private int     travelDistance = DEFAULT_TRAVEL_DISTANCE;
    private int     range          = DEFAULT_RANGE;
    private boolean moving         = false;
    private boolean deploying      = true;  // true=展開方向, false=格納方向
    private double  moved          = 0.0;   // 今回の移動で動いた累積量

    /** テクスチャ偽装ブロック */
    private IBlockState mimicState = null;

    // -------------------------------------------------------
    // ITickable
    // -------------------------------------------------------
    @Override
    public void update() {
        if (world == null || world.isRemote || !moving) return;
        tickJBD();
    }

    // -------------------------------------------------------
    // JBD毎tick処理（Y+Z同時移動で45°の斜め展開）
    // -------------------------------------------------------
    private void tickJBD() {
        if (moved >= travelDistance) {
            moving = false;
            moved  = 0.0;
            syncToClient();
            return;
        }
        double step = Math.min(moveSpeed, travelDistance - moved);
        moved += step;

        // 展開: Y上昇 + Z後退、格納: Y下降 + Z前進
        double dy = deploying ?  step : -step;
        double dz = deploying ? -step :  step;
        moveEntities(dy, dz);
    }

    // -------------------------------------------------------
    // 操作メソッド
    // -------------------------------------------------------

    /** 展開（Y+Z方向に斜め移動） */
    public void deploy() {
        if (moving) return;
        deploying = true;
        moved     = 0.0;
        moving    = true;
        syncToClient();
        McHeliNavalAddon.logger.info("JBD deploy at {}", pos);
    }

    /** 格納（展開と逆方向） */
    public void retract() {
        if (moving) return;
        deploying = false;
        moved     = 0.0;
        moving    = true;
        syncToClient();
        McHeliNavalAddon.logger.info("JBD retract at {}", pos);
    }

    /** 手動トグル（展開↔格納） */
    public void toggle() {
        if (deploying) { retract(); } else { deploy(); }
    }

    // -------------------------------------------------------
    // エンティティ移動（プレイヤー + MCHELI機体）
    // -------------------------------------------------------
    private void moveEntities(double dy, double dz) {
        if (world == null) return;
        AxisAlignedBB box = new AxisAlignedBB(
            pos.getX() - range, pos.getY() - 1,          pos.getZ() - range,
            pos.getX() + range, pos.getY() + range + 3,  pos.getZ() + range
        );
        for (Entity e : world.getEntitiesWithinAABB(Entity.class, box)) {
            if (e instanceof EntityPlayer || McheliReflect.isMcheliAircraft(e)) {
                e.setPosition(e.posX, e.posY + dy, e.posZ + dz);
            }
        }
    }

    // -------------------------------------------------------
    // ゲッター
    // -------------------------------------------------------
    public boolean isMoving()    { return moving; }
    public boolean isDeployed()  { return deploying && moved >= travelDistance; }

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
        tag.setDouble("moveSpeed",      moveSpeed);
        tag.setInteger("travelDistance",travelDistance);
        tag.setInteger("range",         range);
        tag.setBoolean("moving",        moving);
        tag.setBoolean("deploying",     deploying);
        tag.setDouble("moved",          moved);
        TileEntityFloorMarker.writeMimicToTag(tag, mimicState);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        moveSpeed      = tag.hasKey("moveSpeed")      ? tag.getDouble("moveSpeed")        : DEFAULT_MOVE_SPEED;
        travelDistance = tag.hasKey("travelDistance") ? tag.getInteger("travelDistance")  : DEFAULT_TRAVEL_DISTANCE;
        range          = tag.hasKey("range")          ? tag.getInteger("range")           : DEFAULT_RANGE;
        moving         = tag.getBoolean("moving");
        deploying      = tag.getBoolean("deploying");
        moved          = tag.getDouble("moved");
        mimicState     = TileEntityFloorMarker.readMimicFromTag(tag);
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
