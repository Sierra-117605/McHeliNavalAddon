package com.mchelinaval.client;

import com.mchelinaval.McHeliNavalAddon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * クライアント専用のイベントハンドラ（現在は未使用）。
 *
 * テクスチャ偽装機能は Shift+右クリック（onBlockActivated）で処理するため、
 * 左クリックイベントの捕捉は不要になった。
 * 将来のクライアント専用処理が必要になった場合ここに追加する。
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = McHeliNavalAddon.MODID, value = Side.CLIENT)
public class ClientEventHandler {
    // 現在イベントハンドラなし
}
