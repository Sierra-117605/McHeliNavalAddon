package com.mchelinaval.gui;

import com.mchelinaval.network.NavalPacketHandler;
import com.mchelinaval.network.PacketPlatformAction;
import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * 移動プラットフォームのGUI画面（クライアント専用）。
 *
 * 【表示内容】
 *   - 現在のモード（エレベーター / JBD）
 *   - 移動状態（移動中 / 停止中）
 *   - 操作ボタン
 *
 * 【ボタン】
 *   エレベーターモード : 「▲ 上へ」「▼ 下へ」
 *   JBDモード         : 「展開 / 格納」
 *   共通              : 「モード切替」「閉じる」
 */
@SideOnly(Side.CLIENT)
public class GuiMovingPlatform extends GuiScreen {

    // GUI画面のサイズ
    private static final int GUI_WIDTH  = 200;
    private static final int GUI_HEIGHT = 140;

    private final TileEntityMovingPlatform platform;
    private final BlockPos pos;

    // ボタンID定数
    private static final int BTN_UP     = 0;
    private static final int BTN_DOWN   = 1;
    private static final int BTN_TOGGLE = 2;
    private static final int BTN_MODE   = 3;
    private static final int BTN_CLOSE  = 4;

    private GuiButton btnUp;
    private GuiButton btnDown;
    private GuiButton btnToggle;
    private GuiButton btnMode;
    private GuiButton btnClose;

    public GuiMovingPlatform(TileEntityMovingPlatform platform, BlockPos pos) {
        this.platform = platform;
        this.pos = pos;
    }

    @Override
    public void initGui() {
        super.initGui();

        // GUI左上の座標（画面中央に配置）
        int left = (width  - GUI_WIDTH)  / 2;
        int top  = (height - GUI_HEIGHT) / 2;

        // 上へ移動ボタン（ELEVATORモード専用）
        btnUp = new GuiButton(BTN_UP, left + 10, top + 45, 80, 20, "▲ 上へ"); // ▲ 上へ

        // 下へ移動ボタン（ELEVATORモード専用）
        btnDown = new GuiButton(BTN_DOWN, left + 10, top + 70, 80, 20, "▼ 下へ"); // ▼ 下へ

        // JBD展開/格納トグルボタン（JBDモード専用）
        btnToggle = new GuiButton(BTN_TOGGLE, left + 10, top + 45, 80, 20, "展開 / 格納"); // 展開 / 格納

        // モード切替ボタン（共通）
        btnMode = new GuiButton(BTN_MODE, left + 105, top + 45, 85, 20, "モード切替"); // モード切替

        // 閉じるボタン（共通）
        btnClose = new GuiButton(BTN_CLOSE, left + (GUI_WIDTH - 80) / 2, top + GUI_HEIGHT - 28, 80, 20, "閉じる"); // 閉じる

        buttonList.add(btnUp);
        buttonList.add(btnDown);
        buttonList.add(btnToggle);
        buttonList.add(btnMode);
        buttonList.add(btnClose);

        refreshButtonVisibility();
    }

    /** モードに合わせてボタンの表示/非表示を切り替える */
    private void refreshButtonVisibility() {
        boolean isElevator = platform.getMode() == TileEntityMovingPlatform.Mode.ELEVATOR;
        btnUp.visible     = isElevator;
        btnDown.visible   = isElevator;
        btnToggle.visible = !isElevator;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_UP:
                // サーバーへ「上へ移動」命令を送る
                NavalPacketHandler.CHANNEL.sendToServer(
                    new PacketPlatformAction(pos, PacketPlatformAction.Action.GO_UP));
                break;

            case BTN_DOWN:
                // サーバーへ「下へ移動」命令を送る
                NavalPacketHandler.CHANNEL.sendToServer(
                    new PacketPlatformAction(pos, PacketPlatformAction.Action.GO_DOWN));
                break;

            case BTN_TOGGLE:
                // サーバーへ「JBDトグル」命令を送る
                NavalPacketHandler.CHANNEL.sendToServer(
                    new PacketPlatformAction(pos, PacketPlatformAction.Action.TOGGLE));
                break;

            case BTN_MODE:
                // サーバーへ「モード切替」命令を送る
                NavalPacketHandler.CHANNEL.sendToServer(
                    new PacketPlatformAction(pos, PacketPlatformAction.Action.CYCLE_MODE));
                // ローカルのTileEntityには即時反映できないので一拍おく
                // （次回右クリックで正しいモードが表示される）
                break;

            case BTN_CLOSE:
                // GUIを閉じる
                mc.player.closeScreen();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // ============ 背景 ============
        int left = (width  - GUI_WIDTH)  / 2;
        int top  = (height - GUI_HEIGHT) / 2;

        // 半透明の黒いオーバーレイ（デフォルトのdrawDefaultBackground相当）
        this.drawDefaultBackground();

        // GUIパネル（グレー）
        drawRect(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFFC6C6C6);
        // パネル上部バー（濃いグレー）
        drawRect(left, top, left + GUI_WIDTH, top + 22, 0xFF555555);
        // パネル下部バー（濃いグレー）
        drawRect(left, top + GUI_HEIGHT - 30, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFF555555);

        // ============ テキスト ============

        // タイトル（白）
        fontRenderer.drawString(
            "Moving Platform",
            left + 8, top + 7,
            0xFFFFFF
        );

        // 現在のモード
        boolean isElevator = platform.getMode() == TileEntityMovingPlatform.Mode.ELEVATOR;
        String modeStr = isElevator ? "モード: エレベーター"  // モード: エレベーター
                                    : "モード: JBD (斜め展開)";       // モード: JBD (斜め展開)
        fontRenderer.drawString(modeStr, left + 10, top + 28, 0x222222);

        // 移動状態
        boolean isMoving = platform.isMoving();
        String statusStr = isMoving ? "▶ 移動中..." : "■ 停止中"; // ▶ 移動中 / ■ 停止中
        int statusColor   = isMoving ? 0x006600 : 0x880000;
        fontRenderer.drawString(statusStr, left + 105, top + 28, statusColor);

        // ELEVATORモードなら現在Y座標を表示
        if (isElevator) {
            String yStr = "Y = " + (int) platform.getCurrentY();
            fontRenderer.drawString(yStr, left + 10, top + 100, 0x222222);
        }

        // ============ ボタン描画 ============
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** ESCキーで閉じられるようにする */
    @Override
    public boolean doesGuiPauseGame() {
        return false; // ゲームを一時停止しない
    }
}
