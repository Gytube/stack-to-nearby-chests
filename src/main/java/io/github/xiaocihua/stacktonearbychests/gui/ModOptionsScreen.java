package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Écran d'options du mod.
 * Remplace ModOptionsScreen (LibGui CottonClientScreen).
 */
@OnlyIn(Dist.CLIENT)
public class ModOptionsScreen extends Screen {

    private final ModOptionsGui gui;

    public ModOptionsScreen(ModOptionsGui gui) {
        super(Component.translatable("stacktonearbychests.options.title"));
        this.gui = gui;
    }

    @Override
    protected void init() {
        gui.init(this, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fond semi-transparent
        renderBackground(graphics, mouseX, mouseY, partialTick);
        gui.render(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (gui.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (gui.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (gui.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (gui.charTyped(c, modifiers)) return true;
        return super.charTyped(c, modifiers);
    }

    @Override
    public void onClose() {
        gui.onClose();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}