package com.mchelinaval.proxy;

import com.mchelinaval.client.TESRCamouflage;
import com.mchelinaval.registry.ModBlocks;
import com.mchelinaval.tileentity.TileEntityElevatorController;
import com.mchelinaval.tileentity.TileEntityFloorMarker;
import com.mchelinaval.tileentity.TileEntityJBDController;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * クライアント専用のプロキシ。
 * サーバーでは呼ばれない処理（TESR登録など）をここで行う。
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerTileEntityRenderers() {
        // 3ブロック全てにテクスチャ偽装レンダラーを登録する
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityFloorMarker.class,
            new TESRCamouflage<>(ModBlocks.FLOOR_MARKER)
        );
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityElevatorController.class,
            new TESRCamouflage<>(ModBlocks.ELEVATOR_CONTROLLER)
        );
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityJBDController.class,
            new TESRCamouflage<>(ModBlocks.JBD_CONTROLLER)
        );
    }
}
