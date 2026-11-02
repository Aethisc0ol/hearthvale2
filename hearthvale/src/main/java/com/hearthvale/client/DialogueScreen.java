package com.hearthvale.client;

import com.hearthvale.network.DialoguePagePayload;
import com.hearthvale.network.DialogueResponsePayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Stardew-style dialogue box: portrait, typewriter text, heart meter, clickable choices. */
public class DialogueScreen extends Screen {
    public final int entityId;
    private DialoguePagePayload page;
    private ResourceLocation skin;
    private int shown;
    private boolean awaiting;

    public DialogueScreen(DialoguePagePayload page) {
        super(Component.literal(page.speaker()));
        this.entityId = page.entityId();
        load(page);
    }

    private void load(DialoguePagePayload p) {
        this.page = p;
        this.skin = NpcRenderer.texture(p.npcId());
        this.shown = 0;
        this.awaiting = false;
    }

    public void setPage(DialoguePagePayload p) {
        load(p);
        rebuildWidgets();
    }

    // ---- layout ----
    private int boxW() { return Math.min(width - 20, 440); }
    private int textW() { return boxW() - 80; }
    private List<FormattedCharSequence> wrapped() { return font.split(Component.literal(page.text()), textW()); }
    private int boxH() { return Math.max(80, 34 + wrapped().size() * (font.lineHeight + 2)); }
    private int boxX() { return (width - boxW()) / 2; }
    private int boxY() { return height - boxH() - 12; }

    @Override
    protected void init() {
        List<String> choices = page.choices();
        int n = choices.size();
        int cw = Math.min(boxW(), 320);
        for (int i = 0; i < n; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal((i + 1) + ". " + choices.get(i)), b -> choose(idx))
                    .bounds(boxX() + boxW() - cw, boxY() - 6 - (n - i) * 22, cw, 20).build());
        }
    }

    private void choose(int idx) {
        if (awaiting) return;
        awaiting = true;
        PacketDistributor.sendToServer(new DialogueResponsePayload(idx));
    }

    private void advance() {
        if (shown < page.text().length()) {
            shown = page.text().length();
            return;
        }
        if (page.choices().isEmpty() && !awaiting) {
            awaiting = true;
            PacketDistributor.sendToServer(new DialogueResponsePayload(-1));
        }
    }

    // ---- input ----
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        advance();
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_SPACE || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            advance();
            return true;
        }
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            int idx = key - GLFW.GLFW_KEY_1;
            if (idx < page.choices().size() && shown >= page.text().length()) {
                choose(idx);
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new DialogueResponsePayload(-2));
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        if (shown < page.text().length()) shown = Math.min(page.text().length(), shown + 2);
    }

    // ---- drawing ----
    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // keep the world visible behind the dialogue box
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int bx = boxX(), by = boxY(), bw = boxW(), bh = boxH();
        g.fill(bx - 2, by - 2, bx + bw + 2, by + bh + 2, 0xFF2B1B0E);
        g.fill(bx, by, bx + bw, by + bh, 0xF2F3E2C0);

        // portrait (face + hat layer from the 64x64 skin)
        int px = bx + 8, py = by + 8, ps = 48;
        g.fill(px - 2, py - 2, px + ps + 2, py + ps + 2, 0xFF2B1B0E);
        RenderSystem.enableBlend();
        g.blit(skin, px, py, ps, ps, 8f, 8f, 8, 8, 64, 64);
        g.blit(skin, px, py, ps, ps, 40f, 8f, 8, 8, 64, 64);

        // hearts
        int full = page.points() / 250;
        int total = Math.max(10, full);
        int hx = px;
        int hy = py + ps + 3;
                for (int i = 0; i < total && i < 14; i++) {
            g.drawString(font, "\u2665", hx + (i % 7) * 8, hy + (i / 7) * 9, i < full ? 0xFFD02020 : 0xFF9A8A70, false);
        }

        // name + text
        int tx = bx + 68;
        g.drawString(font, page.speaker(), tx, by + 7, 0xFF8B3A0F, false);
        String visible = page.text().substring(0, Math.min(shown, page.text().length()));
        List<FormattedCharSequence> lines = font.split(Component.literal(visible), textW());
        int y = by + 20;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, tx, y, 0xFF3B2A14, false);
            y += font.lineHeight + 2;
        }
        if (shown >= page.text().length() && page.choices().isEmpty() && (System.currentTimeMillis() / 400) % 2 == 0) {
            g.drawString(font, "\u25BC", bx + bw - 14, by + bh - 12, 0xFF8B3A0F, false);
        }
        super.render(g, mx, my, pt);
    }
}
