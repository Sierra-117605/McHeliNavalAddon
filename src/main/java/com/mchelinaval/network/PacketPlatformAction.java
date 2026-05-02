package com.mchelinaval.network;

import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * GUIのボタンを押したときにクライアントからサーバーへ送るパケット。
 * どのブロック位置の、どのアクションかを伝える。
 */
public class PacketPlatformAction implements IMessage {

    /** GUIボタンの操作種別 */
    public enum Action { GO_UP, GO_DOWN, TOGGLE, CYCLE_MODE }

    // パケットに含めるデータ（ブロックのXYZ座標とアクション番号）
    private int x, y, z;
    private int actionOrdinal;

    /** デシリアライズ用（引数なしコンストラクタが必須） */
    public PacketPlatformAction() {}

    /** ボタンが押されたとき呼ぶコンストラクタ */
    public PacketPlatformAction(BlockPos pos, Action action) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.actionOrdinal = action.ordinal();
    }

    /** バイト列からデータを読み込む（受信時） */
    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        actionOrdinal = buf.readInt();
    }

    /** データをバイト列に書き込む（送信時） */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(actionOrdinal);
    }

    /**
     * サーバー側でパケットを受け取ったときの処理。
     * 指定座標のTileEntityに対してアクションを実行する。
     */
    public static class Handler implements IMessageHandler<PacketPlatformAction, IMessage> {

        @Override
        public IMessage onMessage(PacketPlatformAction message, MessageContext ctx) {
            // サーバーのメインスレッドで安全に実行するためaddScheduledTaskを使う
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                BlockPos pos = new BlockPos(message.x, message.y, message.z);
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TileEntityMovingPlatform)) return;

                TileEntityMovingPlatform platform = (TileEntityMovingPlatform) te;
                Action action = Action.values()[message.actionOrdinal];

                switch (action) {
                    case GO_UP:
                        platform.goUp(ctx.getServerHandler().player);
                        break;
                    case GO_DOWN:
                        platform.goDown(ctx.getServerHandler().player);
                        break;
                    case TOGGLE:
                        platform.toggle();
                        break;
                    case CYCLE_MODE:
                        platform.cycleMode();
                        break;
                }
            });
            return null; // 返信パケットなし
        }
    }
}
