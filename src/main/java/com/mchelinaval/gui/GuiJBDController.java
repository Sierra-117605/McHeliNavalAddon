package com.mchelinaval.gui;

import com.mchelinaval.network.NavalPacketHandler;
import com.mchelinaval.network.PacketPlatformAction;
import com.mchelinaval.tileentity.TileEntityJBDController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * JBDコントローラーのGUI。
 *
 * 【ボタン】
 *   展開   : デフレクターをY+Z方向に斜め展開
 *   格納   : 展開と逆方向に格納
 *   閉じる : GUIを閉じる
 */
@SideOnly(Side.CLIENT)
public class GuiJBDController extends GuiScreen {

    private static final int GUI_WIDTH  = 200;
    private static final int GUI_HEIGHT = 120;

    private static final int BTN_DEPLOY  = 0;
    private static final int BTN_RETRACT = 1;
    private static final int BTN_CLOSE   = 2;

    private final TileEntityJBDController te;
    private final BlockPos pos;

    public GuiJBDController(TileEntityJBDController te, BlockPos pos) {
        this.te  = te;
        this.pos = pos;
    }

    @Override
    public void initGui() {
        super.initGui();
        int left = (width  - GUI_WIDTH)  / 2;
        int top  = (height - GUI_HEIGHT) / 2;

        buttonList.add(new GuiButton(BTN_DEPLOY,  left + 10,  top + 45, 80, 20, "展開")); // 展開
        buttonList.add(new GuiButton(BTN_RETRACT, left + 105, top + 45, 80, 20, "格納")); // 格納
        buttonList.add(new GuiButton(BTN_CLOSE,   left + (GUI_WIDTH - 80) / 2, top + GUI_HEIGHT - 28, 80, 20, "閉じる")); // 閉じる
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_DEPLOY:
                NavalPacketHandler.CHANNEL.sendToServer(
                    new PacketPlatformAction(pos, PacketPlatformAction.Action.JBD_DEPLOY));
                break;
            case BTN_RETRACT:
                NavalPacketHandler.CHANNEL.sendToServer(
                    new PacketPlatformAction(pos, PacketPlatformAction.Action.JBD_RETRACT));
                break;
            case BTN_CLOSE:
                mc.player.closeScreen();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int left = (width  - GUI_WIDTH)  / 2;
        int top  = (height - GUI_HEIGHT) / 2;

        drawDefaultBackground();
        drawRect(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFFC6C6C6);
        drawRect(left, top, left + GUI_WIDTH, top + 22, 0xFF555555);
        drawRect(left, top + GUI_HEIGHT - 30, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFF555555);

        fontRenderer.drawString("JBD Controller", left + 8, top + 7, 0xFFFFFF);

        boolean isMoving = te.isMoving();
        String status = isMoving ? "▶ 移動中..." : "■ 停止中"; // ▶ 移動中 / ■ 停止中
        fontRenderer.drawString(status, left + 10, top + 28, isMoving ? 0x006600 : 0x880000);

        fontRenderer.drawString("← 展開: Y+Z斜め移動 | 格納: 逆方向 →", left + 10, top + 75, 0x444444); // 展開・格納の説明

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
