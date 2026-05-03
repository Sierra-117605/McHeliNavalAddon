package com.mchelinaval.client;

import com.mchelinaval.tileentity.IHasMimic;
import net.minecraft.block.Block;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * テクスチャ偽装レンダラー（TESR）。
 *
 * ブロックのRenderTypeをINVISIBLEにした代わりに、このTESRが描画を担う。
 * ・偽装ブロックが設定されている場合 → そのブロックの見た目で描画
 * ・設定されていない場合          → フォールバックブロック（本来の姿）で描画
 *
 * これにより「左クリックで艦船の床と同じ見た目に変える」機能が実現される。
 */
@SideOnly(Side.CLIENT)
public class TESRCamouflage<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

    /** 偽装なし時に使うデフォルトのブロック */
    private final Block fallbackBlock;

    public TESRCamouflage(Block fallbackBlock) {
        this.fallbackBlock = fallbackBlock;
    }

    @Override
    public void render(T te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        // 描画するブロック状態を決定
        IBlockState renderState;
        if (te instanceof IHasMimic) {
            IBlockState mimic = ((IHasMimic) te).getMimicState();
            renderState = (mimic != null) ? mimic : fallbackBlock.getDefaultState();
        } else {
            renderState = fallbackBlock.getDefaultState();
        }

        // ======= ブロックモデルをTESSELLATORで描画 =======
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);         // カメラからの相対位置へ移動
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(7, DefaultVertexFormats.BLOCK);   // 7 = GL_QUADS

        brd.getBlockModelRenderer().renderModel(
            te.getWorld(),                                               // ワールド（光計算用）
            brd.getBlockModelShapes().getModelForState(renderState),     // 描画するモデル
            renderState,                                                 // ブロック状態
            te.getPos(),                                                 // ワールド座標（光量計算用）
            buf,
            false                                                        // AO計算スキップ
        );

        tess.draw();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /** 影（シャドウ）は不要（ブロック自体が影を持つため） */
    @Override
    public boolean isGlobalRenderer(T te) {
        return false;
    }
}
