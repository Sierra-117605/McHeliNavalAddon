package com.mchelinaval.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * 移動プラットフォームのコンテナ。
 * アイテムスロットは持たない。GUI表示のための枠組みとして必要。
 */
public class ContainerMovingPlatform extends Container {

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true; // 誰でも操作可能
    }
}
