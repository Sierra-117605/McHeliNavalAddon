package com.mchelinaval.network;

import com.mchelinaval.McHeliNavalAddon;
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

    public enum Action {
        ELEVATOR_UP,
        ELEVATOR_DOWN,
        JBD_DEPLOY,
        JBD_RETRACT
    }

    private int x, y, z;
    private int actionOrdinal;

    public PacketPlatformAction() {}

    public PacketPlatformAction(BlockPos pos, Action action) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.actionOrdinal = action.ordinal();
        McHeliNavalAddon.logger.info("[Packet] PacketPlatformAction 作成: action={} @ ({},{},{})",
            action, x, y, z);
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
            Action action = Action.values()[message.actionOrdinal];
            McHeliNavalAddon.logger.info("[Packet] PacketPlatformAction 受信: action={} @ ({},{},{})",
                action, message.x, message.y, message.z);

            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world   = ctx.getServerHandler().player.world;
                EntityPlayer player = ctx.getServerHandler().player;
                BlockPos pos  = new BlockPos(message.x, message.y, message.z);
                TileEntity te = world.getTileEntity(pos);

                McHeliNavalAddon.logger.info("[Packet] スケジュール実行: action={} @ {} TE={}",
                    action, pos, te == null ? "null!" : te.getClass().getSimpleName());

                if (te instanceof TileEntityElevatorController) {
                    TileEntityElevatorController elev = (TileEntityElevatorController) te;
                    if (action == Action.ELEVATOR_UP) {
                        McHeliNavalAddon.logger.info("[Packet] → elev.goUp()");
                        elev.goUp(player);
                    } else if (action == Action.ELEVATOR_DOWN) {
                        McHeliNavalAddon.logger.info("[Packet] → elev.goDown()");
                        elev.goDown(player);
                    }

                } else if (te instanceof TileEntityJBDController) {
                    TileEntityJBDController jbd = (TileEntityJBDController) te;
                    if (action == Action.JBD_DEPLOY) {
                        McHeliNavalAddon.logger.info("[Packet] → jbd.deploy()");
                        jbd.deploy();
                    } else if (action == Action.JBD_RETRACT) {
                        McHeliNavalAddon.logger.info("[Packet] → jbd.retract()");
                        jbd.retract();
                    }

                } else {
                    McHeliNavalAddon.logger.warn("[Packet] TEが期待型でない → 何もしない (action={} TE={})",
                        action, te == null ? "null" : te.getClass().getSimpleName());
                }
            });
            return null;
        }
    }
}
