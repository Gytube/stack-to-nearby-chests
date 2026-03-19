package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Ligne horizontale : [Label]  [EditBox]  [Reset]
 * Remplace TextFieldWithLabel (LibGui WBoxCustom + WLabel + WTextField + WButton).
 */
public class TextFieldWithLabel extends AbstractWidget {

    private static final int RESET_W     = 55;
    private static final int FIELD_W     = 40;
    private static final int PADDING     = 2;

    private final Component label;
    private final EditBox   textField;
    private final FlatColorButton resetButton;
    private Component tooltip;

    public TextFieldWithLabel(Component label, int color, Supplier<Integer> onReset) {
        super(0, 0, 0, 20, label);
        this.label = label;

        var font = Minecraft.getInstance().font;

        textField = new EditBox(font, 0, 0, FIELD_W, height, Component.empty());

        resetButton = new FlatColorButton(
                Component.translatable("stacktonearbychests.options.reset"),
                btn -> textField.setValue(String.valueOf(onReset.get())));
        resetButton.setBorder();
    }

    public TextFieldWithLabel withTooltip(String tooltipKey) {
        this.tooltip = Component.translatable(tooltipKey);
        return this;
    }

    /** Positionne les sous-widgets selon la position et largeur courante. */
    public void layout() {
        var font      = Minecraft.getInstance().font;
        int labelW    = font.width(label) + 7;
        int x         = getX();
        int y         = getY();

        textField.setPosition(x + labelW + PADDING, y);
        textField.setWidth(FIELD_W);
        textField.setHeight(height);

        resetButton.setPosition(x + labelW + FIELD_W + PADDING * 2, y);
        resetButton.setSize(RESET_W, height);
    }

    public EditBox getTextField() {
        return textField;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var font   = Minecraft.getInstance().font;
        int labelW = font.width(label) + 7;

        // Label
        graphics.drawString(font, label, getX(), getY() + (height - 8) / 2,
                ModOptionsGui.TEXT_COLOR, false);

        // Tooltip au survol du label
        if (tooltip != null && mouseX >= getX() && mouseX < getX() + labelW
                && mouseY >= getY() && mouseY < getY() + height) {
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }

        textField.render(graphics, mouseX, mouseY, partialTick);
        resetButton.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (textField.mouseClicked(mouseX, mouseY, button)) return true;
        if (resetButton.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return textField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return textField.charTyped(c, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}