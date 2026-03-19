package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.xiaocihua.stacktonearbychests.KeySequence;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Ligne de configuration d'une touche :
 *   [Label texte]  [Widget de touche]  [Bouton Reset]
 */
public class KeymapEntry extends AbstractWidget {

    private static final int KEYBINDING_WIDTH  = 165;
    private static final int RESET_BUTTON_WIDTH = 55;

    private final Component label;
    private final KeyBindingWidget keybinding;
    private final FlatColorButton resetButton;

    public KeymapEntry(Component label, KeySequence keySequence) {
        super(0, 0, 0, 20, label);
        this.label       = label;
        this.keybinding  = new KeyBindingWidget(keySequence);
        this.resetButton = new FlatColorButton(
                Component.translatable("stacktonearbychests.options.reset"),
                btn -> keybinding.reset());
        resetButton.setBorder();
    }

    /** Positionne les sous-widgets. Appeler après setPosition/setWidth. */
    public void layout() {
        int textWidth   = width - KEYBINDING_WIDTH - RESET_BUTTON_WIDTH - 4;
        int kbX         = getX() + textWidth + 2;
        int resetX      = kbX + KEYBINDING_WIDTH + 2;

        keybinding.setPosition(kbX,    getY());
        keybinding.setSize(KEYBINDING_WIDTH, height);

        resetButton.setPosition(resetX, getY());
        resetButton.setSize(RESET_BUTTON_WIDTH, height);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Label
        int textWidth = width - KEYBINDING_WIDTH - RESET_BUTTON_WIDTH - 4;
        graphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                label,
                getX(), getY() + (height - 8) / 2,
                ModOptionsGui.TEXT_COLOR, false);

        keybinding.render(graphics, mouseX, mouseY, partialTick);
        resetButton.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (keybinding.mouseClicked(mouseX, mouseY, button)) return true;
        if (resetButton.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keybinding.isFocused()) {
            return keybinding.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    // ── Widget de touche ─────────────────────────────────────────────────────────

    private static class KeyBindingWidget extends FlatColorButton {

        private final KeySequence keySequence;

        public KeyBindingWidget(KeySequence keySequence) {
            super(Component.empty(), 0xFF_262626, 0x00_000000, 0x00_000000, btn -> {});
            this.keySequence = keySequence;
            refreshLabel();
        }

        public void reset() {
            keySequence.reset();
            refreshLabel();
        }

        @Override
        public void onPress() {
            if (!isFocused()) {
                setFocused(true);
            }
            refreshLabel();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isHovered()) return false;
            if (isFocused()) {
                keySequence.addMouseButton(button);
                refreshLabel();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!isFocused()) return false;
            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER     -> setFocused(false);
                case GLFW.GLFW_KEY_BACKSPACE -> keySequence.clear();
                default                      -> keySequence.addKey(keyCode);
            }
            refreshLabel();
            return true;
        }

        private void refreshLabel() {
            setMessage(keySequence.getLocalizedText());
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isFocused()) {
                drawBorder(graphics, getX(), getY(), width, height, 0xFF_F5F5F5);
            }
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}