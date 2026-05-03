package com.mchelinaval.network;

import com.mchelinaval.McHeliNavalAddon;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * MOD内ネットワーク通信の管理クラス。
 * GUIのボタンを押したとき、クライアント→サーバーへ命令を送る仕組みを提供する。
 */
public class NavalPacketHandler {

    /** MOD専用の通信チャンネル */
    public static final SimpleNetworkWrapper CHANNEL =
        NetworkRegistry.INSTANCE.newSimpleChannel(McHeliNavalAddon.MODID);

    /** パケットを登録する（起動時に一度だけ呼ぶ） */
    public static void register() {
        // PacketPlatformAction: ID=0, ボタン操作（上下・展開格納）
        CHANNEL.registerMessage(
            PacketPlatformAction.Handler.class,
            PacketPlatformAction.class,
            0,
            Side.SERVER
        );
        // PacketSetMimic: ID=1, テクスチャ偽装変更
        CHANNEL.registerMessage(
            PacketSetMimic.Handler.class,
            PacketSetMimic.class,
            1,
            Side.SERVER
        );
    }
}
