package com.mchelinaval.client;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.block.BlockElevatorController;
import com.mchelinaval.block.BlockFloorMarker;
import com.mchelinaval.block.BlockJBDController;
import com.mchelinaval.network.NavalPacketHandler;
import com.mchelinaval.network.PacketSetMimic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * クライアント専用のイベントハンドラ。
 *
 * 【テクスチャ偽装の左クリック処理】
 *   - フロアマーカー / エレベーターコントローラー / JBDコントローラーを
 *     ブロックを持った状態で左クリック → そのブロックのテクスチャに変化する
 *   - 何も持たずに左クリック → 元のテクスチャに戻す（偽装解除）
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = McHeliNavalAddon.MODID, value = Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        World world = event.getWorld();
        if (!world.isRemote) return;   // クライアント側のみ処理する

        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // 対象ブロックかチェック
        if (!(block instanceof BlockFloorMarker) &&
            !(block instanceof BlockElevatorController) &&
            !(block instanceof BlockJBDController)) return;

        // ブロック破壊をキャンセル（左クリックしてもブロックが壊れないようにする）
        event.setCanceled(true);

        // 持っているブロックのIDを取得（空手 or 非ブロックなら 0 = 偽装解除）
        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = player.getHeldItem(event.getHand());

        int blockId = 0; // 0 = 偽装解除
        if (!held.isEmpty() && held.getItem() instanceof ItemBlock) {
            blockId = Block.getIdFromBlock(((ItemBlock) held.getItem()).getBlock());
        }

        // サーバーへ偽装命令を送る
        NavalPacketHandler.CHANNEL.sendToServer(new PacketSetMimic(pos, blockId));
    }
}
