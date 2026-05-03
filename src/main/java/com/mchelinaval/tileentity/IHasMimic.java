package com.mchelinaval.tileentity;

import net.minecraft.block.state.IBlockState;

/**
 * テクスチャ偽装（カモフラージュ）機能を持つTileEntityが実装するインターフェース。
 * 左クリックで持っているブロックのテクスチャに変更できる。
 */
public interface IHasMimic {
    /** 現在設定されている偽装ブロック状態を取得（nullなら偽装なし） */
    IBlockState getMimicState();

    /** 偽装ブロックをセット（nullで偽装解除） */
    void setMimicState(IBlockState state);
}
