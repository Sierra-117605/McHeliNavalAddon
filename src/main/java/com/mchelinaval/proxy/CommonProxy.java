package com.mchelinaval.proxy;

/**
 * サーバー・クライアント共通のプロキシ。
 * クライアント専用処理（TESR登録など）は ClientProxy でオーバーライドする。
 */
public class CommonProxy {

    /** TileEntitySpecialRenderer（テクスチャ偽装レンダラー）を登録する */
    public void registerTileEntityRenderers() {
        // サーバー側では何もしない
    }
}
