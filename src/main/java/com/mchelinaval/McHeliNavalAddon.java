package com.mchelinaval;

import com.mchelinaval.config.NavalConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("{} preInit start", NAME);

        // config/mchelinaval.cfg を読み込む
        File configFile = new File(event.getModConfigurationDirectory(), "mchelinaval.cfg");
        NavalConfig.init(configFile);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("{} init complete", NAME);
    }
}
