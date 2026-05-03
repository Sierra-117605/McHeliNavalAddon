package com.mchelinaval.client;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * テクスチャ偽装レンダラー（TESR）。
 *
 * 偽装ブロックが設定されている場合のみ動作する。
 * （設定なしの場合はブロック自体のモデルが通常通り描画されるため何もしない）
 *
 * ブロックのRenderTypeがINVISIBLEになったとき（disguised=true時）、
 * このTESRが偽装先のブロックテクスチャを代わりに描画する。
 */
@SideOnly(Side.CLIENT)
public class TESRCamouflage<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

    public TESRCamouflage() {}

    @Override
    public void render(T te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        // 偽装なしなら何もしない（ブロックのモデルが普通に描画される）
        if (!(te instanceof IHasMimic)) return;
        IBlockState mimic = ((IHasMimic) te).getMimicState();
        if (mimic == null) return;

        // ======= 偽装ブロックのモデルを描画 =======
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(7, DefaultVertexFormats.BLOCK); // 7 = GL_QUADS
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
