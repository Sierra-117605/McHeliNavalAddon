"""
テクスチャ生成・変換スクリプト。
pip install Pillow

【使い方】
1. 仮テクスチャ生成（Gemini画像が届く前の仮置き）:
   python gen_textures.py

2. Geminiで生成した512x512画像を16x16に変換して配置:
   python gen_textures.py convert <入力ファイル> <出力ファイル名>
   例: python gen_textures.py convert catapult_top_512.png catapult_top.png
"""
import os
import sys

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Pillowがありません。pip install Pillow を実行してください。")
    exit(1)

OUT_DIR = "src/main/resources/assets/mchelinaval/textures/blocks"
os.makedirs(OUT_DIR, exist_ok=True)

# -------------------------------------------------------
# Gemini画像を16x16に縮小して配置
# -------------------------------------------------------
def convert(src_path, dst_name):
    if not os.path.exists(src_path):
        print(f"ファイルが見つかりません: {src_path}")
        return
    img = Image.open(src_path).convert("RGBA")
    img = img.resize((16, 16), Image.LANCZOS)  # 高品質縮小
    dst = os.path.join(OUT_DIR, dst_name)
    img.save(dst)
    print(f"変換完了: {src_path} → {dst} (16x16)")

# -------------------------------------------------------
# 仮テクスチャ生成（Geminiが届くまでの仮置き）
# -------------------------------------------------------
def make_temp(filename, base_color, pattern="none", accent=None):
    img = Image.new("RGBA", (16, 16), base_color)
    draw = ImageDraw.Draw(img)

    if pattern == "rail":
        # 縦中央に打ち出しレール（カタパルト上面）
        draw.rectangle([6, 0, 9, 15], fill=accent)
        draw.rectangle([5, 6, 10, 9], fill=accent)  # 中央マーク
    elif pattern == "stripes":
        # 黄色安全ストライプ（プラットフォーム上面）
        for i in range(0, 16, 4):
            draw.line([(0, i), (15, i)], fill=accent, width=1)
    elif pattern == "panel":
        # 工業パネル（側面）
        draw.rectangle([1, 1, 14, 14], outline=accent, width=1)
        draw.line([(0, 7), (15, 7)], fill=accent, width=1)
    elif pattern == "bolts":
        # ボルト（底面）
        for bx, by in [(2,2),(13,2),(2,13),(13,13),(7,7)]:
            draw.rectangle([bx-1, by-1, bx+1, by+1], fill=accent)

    draw.rectangle([0, 0, 15, 15], outline=(0, 0, 0, 180))
    img.save(os.path.join(OUT_DIR, filename))
    print(f"  仮生成: {filename}")

def generate_temps():
    print("仮テクスチャ生成中...")

    IRON    = (100, 100, 110, 255)
    DARK    = (60,  60,  68,  255)
    YELLOW  = (220, 200, 50,  255)
    GRAY_LT = (140, 140, 150, 255)

    # カタパルト
    make_temp("catapult_top.png",    IRON,  "rail",   YELLOW)   # 上面: レール
    make_temp("catapult_side.png",   DARK,  "panel",  GRAY_LT)  # 側面: 機械パネル
    make_temp("catapult_bottom.png", DARK,  "bolts",  GRAY_LT)  # 底面: ボルト

    # 移動プラットフォーム
    make_temp("moving_platform_top.png",    IRON, "stripes", YELLOW)   # 上面: 安全ストライプ
    make_temp("moving_platform_side.png",   DARK, "panel",   GRAY_LT)  # 側面: パネル
    make_temp("moving_platform_bottom.png", DARK, "bolts",   GRAY_LT)  # 底面: ボルト

    print("完了。Gemini画像が届いたら convert コマンドで差し替えてください。")

# -------------------------------------------------------
# エントリポイント
# -------------------------------------------------------
if __name__ == "__main__":
    if len(sys.argv) >= 4 and sys.argv[1] == "convert":
        convert(sys.argv[2], sys.argv[3])
    else:
        generate_temps()
