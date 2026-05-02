package com.mchelinaval.registry;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.block.BlockCatapult;
import com.mchelinaval.block.BlockMovingPlatform;
import com.mchelinaval.tileentity.TileEntityCatapult;
import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
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
@Mod.EventBusSubscriber(modid = McHeliNavalAddon.MODID)
public class ModBlocks {

    // ブロックのインスタンスを静的に保持（他クラスから参照できるようにする）
    public static BlockCatapult CATAPULT;
    public static BlockMovingPlatform MOVING_PLATFORM;

    // クリエイティブタブ
    // createIcon()はゲーム起動後に呼ばれるため、その時点でCATAPULTは登録済みになっている
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("mchelinaval") {
        @Override
        public net.minecraft.item.ItemStack createIcon() {
            // CATAPULTがまだnullの場合は空のItemStackを返してクラッシュを防ぐ
            if (CATAPULT == null) return net.minecraft.item.ItemStack.EMPTY;
            return new net.minecraft.item.ItemStack(CATAPULT);
        }
    };

    // -------------------------------------------------------
    // ブロック登録イベント
    // -------------------------------------------------------
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        CATAPULT         = new BlockCatapult();
        MOVING_PLATFORM  = new BlockMovingPlatform();

        // クリエイティブタブをセット
        CATAPULT.setCreativeTab(CREATIVE_TAB);
        MOVING_PLATFORM.setCreativeTab(CREATIVE_TAB);

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
