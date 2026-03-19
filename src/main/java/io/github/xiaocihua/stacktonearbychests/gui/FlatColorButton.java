package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.OptionalInt;

/**
 * Remplace FlatColorButton (LibGui WButton) par un Button vanilla NeoForge.
 * Rendu plat avec couleur de fond configurable et bordure optionnelle.
 */
public class FlatColorButton extends Button {

    private int regularColor  = 0x00_000000;
    private int hoveredColor  = 0x14_FFFFFF;
    private int disabledColor = 0x00_000000;
    private OptionalInt borderColor = OptionalInt.empty();

    // ── Constructeurs ────────────────────────────────────────────────────────────

    public FlatColorButton(Component text, OnPress onPress) {
        super(Button.builder(text, onPress).size(20, 20));
    }

    public FlatColorButton(Component text, int regularColor, int hoveredColor, int disabledColor, OnPress onPress) {
        this(text, onPress);
        this.regularColor  = regularColor;
        this.hoveredColor  = hoveredColor;
        this.disabledColor = disabledColor;
    }

    /** Raccourci sans action (utilisé quand onClick est géré par sous-classe). */
    public static FlatColorButton noOp(Component text) {
        return new FlatColorButton(text, btn -> {});
    }

    // ── Fluent setters ───────────────────────────────────────────────────────────

    public FlatColorButton setBorder() {
        return setBorder(0xFF_717171);
    }

    public FlatColorButton setBorder(int color) {
        borderColor = OptionalInt.of(color);
        return this;
    }

    public FlatColorButton colors(int regular, int hovered, int disabled) {
        this.regularColor  = regular;
        this.hoveredColor  = hovered;
        this.disabledColor = disabled;
        return this;
    }

    // ── Rendu ────────────────────────────────────────────────────────────────────

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHovered();
        boolean enabled = isActive();

        int bg = !enabled ? disabledColor : hovered || isFocused() ? hoveredColor : regularColor;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);

        borderColor.ifPresent(color -> drawBorder(graphics, getX(), getY(), width, height, color));

        // Texte centré
        int textColor = enabled ? 0xFF_E0E0E0 : 0xFF_A0A0A0;
        graphics.drawCenteredString(
                net.minecraft.client.Minecraft.getInstance().font,
                getMessage(),
                getX() + width / 2,
                getY() + (height - 8) / 2,
                textColor);
    }

    protected void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,          x + w,     y + 1,     color); // top
        g.fill(x,         y + h - 1,  x + w,     y + h,     color); // bottom
        g.fill(x,         y,          x + 1,     y + h,     color); // left
        g.fill(x + w - 1, y,          x + w,     y + h,     color); // right
    }
}