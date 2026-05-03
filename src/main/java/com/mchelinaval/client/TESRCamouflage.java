package com.mchelinaval.client;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.tileentity.IHasMimic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * テクスチャ偽装レンダラー（TESR）。
 *
 * 偽装ブロックが設定されている場合のみ動作する。
 *
 * 【座標計算の重要な注意点】
 *   renderModel の第4引数（BlockPos）は以下の2つに使われる：
 *     (A) 光レベル・AO計算 → 実際のブロック位置 te.getPos() を渡す必要がある
 *     (B) バッファへの頂点オフセット → ワールド座標分だけ頂点がズレる
 *
 *   (B) のズレを補正するため、GL translate を
 *     「カメラ相対オフセット(x,y,z) - ワールド座標(posX,posY,posZ)」
 *   に設定する。こうすると頂点が (posX + [0..1]) で出力されても
 *   最終的に (x + [0..1]) = 正しい画面位置に来る。
 *
 *   ※ BlockPos.ORIGIN を使うと頂点位置は正しいが地底(0,0,0)の光を
 *     参照するため真っ黒になる。
 */
@SideOnly(Side.CLIENT)
public class TESRCamouflage<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

    public TESRCamouflage() {}

    @Override
    public void render(T te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        if (!(te instanceof IHasMimic)) return;
        IBlockState mimic = ((IHasMimic) te).getMimicState();
        if (mimic == null) return;

        // ======= 偽装ブロックのモデルを描画 =======
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        BlockPos blockPos = te.getPos();

        GlStateManager.pushMatrix();
        // te.getPos() を renderModel に渡すため、その分 GL 座標をずらして補正する
        // 補正後の頂点位置: (posX + frac) + (x - posX) = x + frac → 正しい画面位置
        GlStateManager.translate(
            x - blockPos.getX(),
            y - blockPos.getY(),
            z - blockPos.getZ()
        );
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(7, DefaultVertexFormats.BLOCK);
        // te.getPos() を渡すことで実際の位置から光レベルを取得する（真っ黒を防ぐ）
        brd.getBlockModelRenderer().renderModel(
            te.getWorld(),
            brd.getBlockModelShapes().getModelForState(mimic),
            mimic,
            te.getPos(),
            buf,
            false
        );
        tess.draw();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
