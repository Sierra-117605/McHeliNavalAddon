package com.mchelinaval;

import com.mchelinaval.config.NavalConfig;
import com.mchelinaval.gui.NavalGuiHandler;
import com.mchelinaval.network.NavalPacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * McHeliNavalAddon — エントリポイント
 * MCHELIに空母甲板装備（カタパルト・移動プラットフォーム）を追加するMOD
 */
@Mod(
    modid = McHeliNavalAddon.MODID,
    name = McHeliNavalAddon.NAME,
    version = McHeliNavalAddon.VERSION,
    dependencies = "required-after:mcheli"
)
public class McHeliNavalAddon {

    public static final String MODID   = "mchelinaval";
    public static final String NAME    = "McHeli Naval Addon";
    public static final String VERSION = "1.0.0";

    /** BlockMovingPlatform から player.openGui() に渡すために必要 */
    @Instance(MODID)
    public static McHeliNavalAddon instance;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("{} preInit start", NAME);

        // config/mchelinaval.cfg を読み込む
        File configFile = new File(event.getModConfigurationDirectory(), "mchelinaval.cfg");
        NavalConfig.init(configFile);

        // ネットワークパケットを登録（GUIボタンの命令をサーバーへ送る仕組み）
        NavalPacketHandler.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // GUIハンドラを登録（右クリックでGUIを開く仕組み）
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new NavalGuiHandler());

        logger.info("{} init complete", NAME);
    }
}
