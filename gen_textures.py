"""
テクスチャ生成スクリプト。
Pillowが必要: pip install Pillow
16x16のシンプルなテクスチャを生成する。
"""
import os
try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Pillowがありません。pip install Pillow を実行してください。")
    exit(1)

OUT_DIR = "src/main/resources/assets/mchelinaval/textures/blocks"
os.makedirs(OUT_DIR, exist_ok=True)

def make_texture(filename, base_color, stripe_color=None, stripe_axis="h"):
    """16x16のテクスチャを生成する。ストライプオプション付き。"""
    img = Image.new("RGBA", (16, 16), base_color)
    draw = ImageDraw.Draw(img)

    if stripe_color:
        # 4px間隔でストライプを描く
        for i in range(0, 16, 4):
            if stripe_axis == "h":
                draw.line([(0, i), (15, i)], fill=stripe_color, width=1)
            else:
                draw.line([(i, 0), (i, 15)], fill=stripe_color, width=1)

    # 枠線（1px）
    draw.rectangle([0, 0, 15, 15], outline=(0, 0, 0, 200))

    path = os.path.join(OUT_DIR, filename)
    img.save(path)
    print(f"  生成: {path}")

print("テクスチャ生成中...")

# カタパルト: 鉄グレー + 黄色ストライプ（射出レール）
make_texture(
    "catapult.png",
    base_color=(90, 90, 100, 255),
    stripe_color=(220, 200, 50, 255),
    stripe_axis="h"
)

# 移動プラットフォーム: ダークグレー + 白ストライプ
make_texture(
    "moving_platform.png",
    base_color=(60, 60, 65, 255),
    stripe_color=(200, 200, 200, 255),
    stripe_axis="v"
)

print("完了！")
