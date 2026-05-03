package com.mchelinaval.tileentity;

import com.mchelinaval.McHeliNavalAddon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

/**
 * フロアマーカーのTileEntity。
 * テクスチャ偽装（カモフラージュ）機能のみを担う。
 * 左クリックで持っているブロックの見た目に変えることができる。
 */
public class TileEntityFloorMarker extends TileEntity implements IHasMimic {

    private IBlockState mimicState = null;

    // -------------------------------------------------------
    // IHasMimic 実装
    // -------------------------------------------------------

    @Override
    public IBlockState getMimicState() {
        return mimicState;
    }

    @Override
    public void setMimicState(IBlockState state) {
        McHeliNavalAddon.logger.info("[FloorMarkerTE] setMimicState @ {} : {} → {}",
            pos,
            mimicState == null ? "null" : mimicState.getBlock().getRegistryName(),
            state      == null ? "null" : state.getBlock().getRegistryName());
        this.mimicState = state;
        markDirty();
        syncToClient();
    }

    // -------------------------------------------------------
    // NBT 保存・読み込み
    // -------------------------------------------------------

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeMimic(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mimicState = readMimic(tag);
    }

    // -------------------------------------------------------
    // クライアント同期
    // -------------------------------------------------------

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    private void syncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            McHeliNavalAddon.logger.info("[FloorMarkerTE] syncToClient 送信 @ {}", pos);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return old.getBlock() != nw.getBlock();
    }

    // -------------------------------------------------------
    // 共通NBTユーティリティ（ミミック読み書き）
    // -------------------------------------------------------

    /** ミミック状態をNBTに書き込む */
    static void writeMimicToTag(NBTTagCompound tag, IBlockState mimic) {
        if (mimic != null && mimic.getBlock() != Blocks.AIR) {
            tag.setString("mimicBlock", mimic.getBlock().getRegistryName().toString());
            tag.setInteger("mimicMeta", mimic.getBlock().getMetaFromState(mimic));
        }
    }

    /** NBTからミミック状態を読み込む */
    static IBlockState readMimicFromTag(NBTTagCompound tag) {
        if (!tag.hasKey("mimicBlock")) return null;
        ResourceLocation rl = new ResourceLocation(tag.getString("mimicBlock"));
        Block block = Block.REGISTRY.getObject(rl);
        if (block == null || block == Blocks.AIR) return null;
        int meta = tag.hasKey("mimicMeta") ? tag.getInteger("mimicMeta") : 0;
        return block.getStateFromMeta(meta);
    }

    private void writeMimic(NBTTagCompound tag) {
        writeMimicToTag(tag, mimicState);
    }

    private IBlockState readMimic(NBTTagCompound tag) {
        return readMimicFromTag(tag);
    }
}
