package com.mchelinaval.network;

import com.mchelinaval.tileentity.TileEntityElevatorController;
import com.mchelinaval.tileentity.TileEntityJBDController;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * GUIのボタン操作をサーバーへ伝えるパケット。
 * エレベーター（上へ・下へ）とJBD（展開・格納）を統一的に扱う。
 */
public class PacketPlatformAction implements IMessage {

    /** ボタン操作の種別 */
    public enum Action {
        ELEVATOR_UP,    // エレベーター: 上のフロアへ
        ELEVATOR_DOWN,  // エレベーター: 下のフロアへ
        JBD_DEPLOY,     // JBD: 展開
        JBD_RETRACT     // JBD: 格納
    }

    private int x, y, z;
    private int actionOrdinal;

    public PacketPlatformAction() {}

    public PacketPlatformAction(BlockPos pos, Action action) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.actionOrdinal = action.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt(); y = buf.readInt(); z = buf.readInt();
        actionOrdinal = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x); buf.writeInt(y); buf.writeInt(z);
        buf.writeInt(actionOrdinal);
    }

    public static class Handler implements IMessageHandler<PacketPlatformAction, IMessage> {

        @Override
        public IMessage onMessage(PacketPlatformAction message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world  = ctx.getServerHandler().player.world;
                EntityPlayer player = ctx.getServerHandler().player;
                BlockPos pos = new BlockPos(message.x, message.y, message.z);
                TileEntity te = world.getTileEntity(pos);
                Action action = Action.values()[message.actionOrdinal];

                if (te instanceof TileEntityElevatorController) {
                    TileEntityElevatorController elev = (TileEntityElevatorController) te;
                    if (action == Action.ELEVATOR_UP)   elev.goUp(player);
                    if (action == Action.ELEVATOR_DOWN) elev.goDown(player);

                } else if (te instanceof TileEntityJBDController) {
                    TileEntityJBDController jbd = (TileEntityJBDController) te;
                    if (action == Action.JBD_DEPLOY)  jbd.deploy();
                    if (action == Action.JBD_RETRACT) jbd.retract();
                }
            });
            return null;
        }
    }
}
