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
 * （設定なしの場合はブロック自体のモデルが通常通り描画されるため何もしない）
 *
 * 【重要】renderModel の第4引数は BlockPos.ORIGIN を渡すこと。
 *   te.getPos()（ワールド座標）を渡すと、GLの座標変換と二重になって
 *   偽装テクスチャがブロックの遥か彼方に描画されてしまう。
 */
@SideOnly(Side.CLIENT)
public class TESRCamouflage<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

    /** ログスパム防止用：最後にログを出したmimicState */
    private IBlockState lastLoggedMimic = null;

    public TESRCamouflage() {
        McHeliNavalAddon.logger.info("[TESR] TESRCamouflage インスタンス生成: {}", this.getClass().getSimpleName());
    }

    @Override
    public void render(T te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        // 偽装なしなら何もしない（ブロックのモデルが普通に描画される）
        if (!(te instanceof IHasMimic)) {
            McHeliNavalAddon.logger.warn("[TESR] render呼び出し: TEがIHasMimicを実装していない → {} @ {}",
                te.getClass().getSimpleName(), te.getPos());
            return;
        }

        IBlockState mimic = ((IHasMimic) te).getMimicState();

        // mimicが変わったときだけログ出力（毎フレームだとスパムになる）
        if (mimic != lastLoggedMimic) {
            lastLoggedMimic = mimic;
            if (mimic == null) {
                McHeliNavalAddon.logger.info("[TESR] {} @ {} : mimic=null → 通常描画（TESR何もしない）",
                    te.getClass().getSimpleName(), te.getPos());
            } else {
                McHeliNavalAddon.logger.info("[TESR] {} @ {} : mimic={} → 偽装描画開始",
                    te.getClass().getSimpleName(), te.getPos(),
                    mimic.getBlock().getRegistryName());
            }
        }

        if (mimic == null) return;

        // ======= 偽装ブロックのモデルを描画 =======
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);  // カメラ相対のブロック位置へ移動
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(7, DefaultVertexFormats.BLOCK); // 7 = GL_QUADS
        // ※ 第4引数は BlockPos.ORIGIN（絶対座標を渡すと二重オフセットになるバグに注意）
        brd.getBlockModelRenderer().renderModel(
            te.getWorld(),
            brd.getBlockModelShapes().getModelForState(mimic),
            mimic,
            BlockPos.ORIGIN,   // ← ここが重要！te.getPos() ではなく ORIGIN
            buf,
            false
        );
        tess.draw();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
