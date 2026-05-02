package com.mchelinaval.registry;

import com.mchelinaval.block.BlockCatapult;
import com.mchelinaval.block.BlockMovingPlatform;
import com.mchelinaval.tileentity.TileEntityCatapult;
import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * ブロックとTileEntityの登録をまとめるクラス。
 * ForgeのRegistryEventを使って登録する（1.12.2の推奨方式）。
 */
@Mod.EventBusSubscriber
public class ModBlocks {

    // ブロックのインスタンスを静的に保持（他クラスから参照できるようにする）
    public static BlockCatapult CATAPULT;
    public static BlockMovingPlatform MOVING_PLATFORM;

    // -------------------------------------------------------
    // ブロック登録イベント
    // -------------------------------------------------------
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        CATAPULT         = new BlockCatapult();
        MOVING_PLATFORM  = new BlockMovingPlatform();

        event.getRegistry().registerAll(
            CATAPULT,
            MOVING_PLATFORM
        );

        // TileEntityも同タイミングで登録する
        GameRegistry.registerTileEntity(TileEntityCatapult.class,       "mchelinaval:catapult_te");
        GameRegistry.registerTileEntity(TileEntityMovingPlatform.class,  "mchelinaval:moving_platform_te");
    }

    // -------------------------------------------------------
    // アイテム登録イベント（ブロックをインベントリに持てるようにする）
    // -------------------------------------------------------
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            new ItemBlock(CATAPULT).setRegistryName(CATAPULT.getRegistryName()),
            new ItemBlock(MOVING_PLATFORM).setRegistryName(MOVING_PLATFORM.getRegistryName())
        );
    }
}
