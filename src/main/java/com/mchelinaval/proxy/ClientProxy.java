package com.mchelinaval.proxy;

import com.mchelinaval.McHeliNavalAddon;
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
        McHeliNavalAddon.logger.info("[ClientProxy] TESR登録開始");

        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityFloorMarker.class, new TESRCamouflage<>());
        McHeliNavalAddon.logger.info("[ClientProxy] TESRCamouflage → TileEntityFloorMarker 登録完了");

        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityElevatorController.class, new TESRCamouflage<>());
        McHeliNavalAddon.logger.info("[ClientProxy] TESRCamouflage → TileEntityElevatorController 登録完了");

        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityJBDController.class, new TESRCamouflage<>());
        McHeliNavalAddon.logger.info("[ClientProxy] TESRCamouflage → TileEntityJBDController 登録完了");

        McHeliNavalAddon.logger.info("[ClientProxy] TESR登録完了（3クラス）");
    }
}
