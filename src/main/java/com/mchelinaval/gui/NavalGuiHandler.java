package com.mchelinaval.gui;

import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * GUIを開くときにForgeが呼び出すハンドラ。
 * サーバー側とクライアント側で別々に呼ばれる。
 *
 * GUI ID:
 *   1 = 移動プラットフォームGUI
 */
public class NavalGuiHandler implements IGuiHandler {

    public static final int GUI_MOVING_PLATFORM = 1;

    /**
     * サーバー側：ContainerをHand（コンテナ）として返す。
     * スロットなしなので ContainerMovingPlatform を返す。
     */
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
                                       int x, int y, int z) {
        if (ID == GUI_MOVING_PLATFORM) {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityMovingPlatform) {
                return new ContainerMovingPlatform();
            }
        }
        return null;
    }

    /**
     * クライアント側：GuiScreenを返す。
     * ここで返したGuiScreenが実際に画面に表示される。
     */
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
                                       int x, int y, int z) {
        if (ID == GUI_MOVING_PLATFORM) {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityMovingPlatform) {
                return new GuiMovingPlatform((TileEntityMovingPlatform) te, pos);
            }
        }
        return null;
    }
}
