package com.mchelinaval.registry;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.block.BlockCatapult;
import com.mchelinaval.block.BlockElevatorController;
import com.mchelinaval.block.BlockFloorMarker;
import com.mchelinaval.block.BlockJBDController;
import com.mchelinaval.tileentity.TileEntityCatapult;
import com.mchelinaval.tileentity.TileEntityElevatorController;
import com.mchelinaval.tileentity.TileEntityFloorMarker;
import com.mchelinaval.tileentity.TileEntityJBDController;
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
 */
@Mod.EventBusSubscriber(modid = McHeliNavalAddon.MODID)
public class ModBlocks {

    public static BlockCatapult            CATAPULT;
    public static BlockElevatorController  ELEVATOR_CONTROLLER;
    public static BlockJBDController       JBD_CONTROLLER;
    public static BlockFloorMarker         FLOOR_MARKER;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("mchelinaval") {
        @Override
        public net.minecraft.item.ItemStack createIcon() {
            if (CATAPULT == null) return net.minecraft.item.ItemStack.EMPTY;
            return new net.minecraft.item.ItemStack(CATAPULT);
        }
    };

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        McHeliNavalAddon.logger.info("[ModBlocks] ブロック登録開始");

        CATAPULT            = new BlockCatapult();
        ELEVATOR_CONTROLLER = new BlockElevatorController();
        JBD_CONTROLLER      = new BlockJBDController();
        FLOOR_MARKER        = new BlockFloorMarker();

        CATAPULT.setCreativeTab(CREATIVE_TAB);
        ELEVATOR_CONTROLLER.setCreativeTab(CREATIVE_TAB);
        JBD_CONTROLLER.setCreativeTab(CREATIVE_TAB);
        FLOOR_MARKER.setCreativeTab(CREATIVE_TAB);

        event.getRegistry().registerAll(
            CATAPULT,
            ELEVATOR_CONTROLLER,
            JBD_CONTROLLER,
            FLOOR_MARKER
        );
        McHeliNavalAddon.logger.info("[ModBlocks] ブロック4種 Registry 登録完了");

        // TileEntityを登録
        GameRegistry.registerTileEntity(TileEntityCatapult.class,          "mchelinaval:catapult_te");
        McHeliNavalAddon.logger.info("[ModBlocks] TE登録: catapult_te");

        GameRegistry.registerTileEntity(TileEntityElevatorController.class, "mchelinaval:elevator_te");
        McHeliNavalAddon.logger.info("[ModBlocks] TE登録: elevator_te");

        GameRegistry.registerTileEntity(TileEntityJBDController.class,      "mchelinaval:jbd_te");
        McHeliNavalAddon.logger.info("[ModBlocks] TE登録: jbd_te");

        GameRegistry.registerTileEntity(TileEntityFloorMarker.class,        "mchelinaval:floor_marker_te");
        McHeliNavalAddon.logger.info("[ModBlocks] TE登録: floor_marker_te");

        McHeliNavalAddon.logger.info("[ModBlocks] 全登録完了（ブロック4種 + TE4種）");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        McHeliNavalAddon.logger.info("[ModBlocks] ItemBlock登録開始");
        event.getRegistry().registerAll(
            new ItemBlock(CATAPULT).setRegistryName(CATAPULT.getRegistryName()),
            new ItemBlock(ELEVATOR_CONTROLLER).setRegistryName(ELEVATOR_CONTROLLER.getRegistryName()),
            new ItemBlock(JBD_CONTROLLER).setRegistryName(JBD_CONTROLLER.getRegistryName()),
            new ItemBlock(FLOOR_MARKER).setRegistryName(FLOOR_MARKER.getRegistryName())
        );
        McHeliNavalAddon.logger.info("[ModBlocks] ItemBlock4種 登録完了");
    }
}
