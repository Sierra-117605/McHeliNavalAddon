package com.mchelinaval.network;

import com.mchelinaval.tileentity.IHasMimic;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * テクスチャ偽装パケット。
 * クライアントが左クリックしたとき、サーバーへ「このブロックに偽装して」と伝える。
 */
public class PacketSetMimic implements IMessage {

    private int x, y, z;
    private int blockId;    // 偽装するブロックのID（0=偽装解除）

    /** デシリアライズ用 */
    public PacketSetMimic() {}

    /**
     * @param pos     偽装対象のブロック座標
     * @param blockId 偽装するブロックのID（Block.getIdFromBlock() で取得）。0で偽装解除
     */
    public PacketSetMimic(BlockPos pos, int blockId) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.blockId = blockId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x       = buf.readInt();
        y       = buf.readInt();
        z       = buf.readInt();
        blockId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(blockId);
    }

    public static class Handler implements IMessageHandler<PacketSetMimic, IMessage> {

        @Override
        public IMessage onMessage(PacketSetMimic message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                BlockPos pos = new BlockPos(message.x, message.y, message.z);
                TileEntity te = world.getTileEntity(pos);

                if (!(te instanceof IHasMimic)) return;

                // blockId=0 → 偽装解除, それ以外 → 該当ブロックに偽装
                IBlockState mimic = null;
                if (message.blockId != 0) {
                    Block block = Block.getBlockById(message.blockId);
                    if (block != null && block != Blocks.AIR) {
                        mimic = block.getDefaultState();
                    }
                }

                ((IHasMimic) te).setMimicState(mimic);
            });
            return null;
        }
    }
}
