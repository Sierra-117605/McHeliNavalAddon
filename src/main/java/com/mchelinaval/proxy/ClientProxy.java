package com.mchelinaval.proxy;

import com.mchelinaval.client.TESRCamouflage;
import com.mchelinaval.tileentity.TileEntityElevatorController;
import com.mchelinaval.tileentity.TileEntityFloorMarker;
import com.mchelinaval.tileentity.TileEntityJBDController;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * クライアント専用のプロキシ。
 * TESR（テクスチャ偽装レンダラー）の登録を行う。
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityFloorMarker.class,         new TESRCamouflage<>());
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityElevatorController.class,  new TESRCamouflage<>());
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityJBDController.class,       new TESRCamouflage<>());
    }
}
