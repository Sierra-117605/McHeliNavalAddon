package com.mchelinaval.config;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.tileentity.TileEntityCatapult;
import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

/**
 * config/mchelinaval.cfg の読み書きを管理するクラス。
 * ゲーム起動時と設定変更時に値を反映する。
 */
@Mod.EventBusSubscriber(modid = McHeliNavalAddon.MODID)
public class NavalConfig {

    private static Configuration config;

    // -------------------------------------------------------
    // 設定項目のデフォルト値
    // -------------------------------------------------------
    private static final float  DEFAULT_LAUNCH_SPEED     = 3.0f;
    private static final double DEFAULT_PLATFORM_SPEED   = 0.05;
    private static final int    DEFAULT_TRAVEL_DISTANCE  = 5;
    private static final int    DEFAULT_PLATFORM_RANGE   = 5;

    // -------------------------------------------------------
    // 初期化（FMLPreInitializationEventのタイミングで呼ぶ）
    // -------------------------------------------------------
    public static void init(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    private static void load() {
        config.load();

        // カタパルト設定
        float launchSpeed = config.getFloat(
            "launchSpeed",
            "catapult",
            DEFAULT_LAUNCH_SPEED,
            0.5f, 20.0f,
            "カタパルトの打ち出し速度（デフォルト: 3.0）"
        );
        TileEntityCatapult.LAUNCH_SPEED = launchSpeed;

        // 移動プラットフォーム設定
        TileEntityMovingPlatform.DEFAULT_MOVE_SPEED = config.getFloat(
            "moveSpeed",
            "movingPlatform",
            (float) DEFAULT_PLATFORM_SPEED,
            0.01f, 1.0f,
            "移動プラットフォームの速度（毎tick移動量。デフォルト: 0.05）"
        );
        TileEntityMovingPlatform.DEFAULT_TRAVEL_DISTANCE = config.getInt(
            "travelDistance",
            "movingPlatform",
            DEFAULT_TRAVEL_DISTANCE,
            1, 32,
            "移動プラットフォームの移動距離（ブロック数。デフォルト: 5）"
        );
        TileEntityMovingPlatform.DEFAULT_RANGE = config.getInt(
            "range",
            "movingPlatform",
            DEFAULT_PLATFORM_RANGE,
            1, 16,
            "移動プラットフォームがエンティティを動かす範囲（ブロック数。デフォルト: 5）"
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    // -------------------------------------------------------
    // ゲーム内から設定変更した時に再読み込み
    // -------------------------------------------------------
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(McHeliNavalAddon.MODID)) {
            load();
        }
    }
}
