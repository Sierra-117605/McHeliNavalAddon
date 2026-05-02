package com.mchelinaval;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

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

    // ログ出力用（他クラスからも使えるようにstaticで持つ）
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("{} preInit start", NAME);

        // ブロック・アイテム登録はここで行う（M2以降で追加）
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("{} init complete", NAME);
    }
}
