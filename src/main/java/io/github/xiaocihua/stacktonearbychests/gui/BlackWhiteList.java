package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Panneau vertical : titre + boutons +/- + liste sélectionnable.
 * Remplace BlackWhiteList (LibGui WBoxCustom).
 */
public class BlackWhiteList extends AbstractWidget {

    private final Component title;
    private final SelectableEntryList<ResourceLocation> list;
    private final FlatColorButton addButton;
    private final FlatColorButton removeButton;

    private static final int TITLE_HEIGHT   = 12;
    private static final int BUTTONS_HEIGHT = 12;
    private static final int SPACING        = 2;

    public BlackWhiteList(Component title,
                          Collection<String> data,
                          Function<ResourceLocation, SelectableEntryList.Entry<ResourceLocation>> entrySupplier,
                          Consumer<Consumer<List<ResourceLocation>>> onAddButtonClick,
                          Consumer<Set<String>> dataChangeListener) {
        super(0, 0, 0, 0, title);
        this.title = title;

        this.list = new SelectableEntryList<>(
                data.stream().map(ResourceLocation::parse).toList(),
                entrySupplier);
        this.list.setChangedListener(ids ->
                dataChangeListener.accept(ids.stream()
                        .map(ResourceLocation::toString)
                        .collect(Collectors.toSet())));

        this.addButton = new FlatColorButton(Component.literal("+"),
                btn -> onAddButtonClick.accept(list::addData));

        this.removeButton = new FlatColorButton(Component.literal("-"),
                btn -> list.removeSelected());
    }

    /** Positionne les sous-widgets selon la taille courante. */
    public void layout() {
        int x = getX();
        int y = getY();
        int w = width;

        int buttonW  = 12;
        int listHeight = height - TITLE_HEIGHT - BUTTONS_HEIGHT - SPACING * 2;

        addButton.setPosition(x, y + TITLE_HEIGHT + SPACING);
        addButton.setSize(buttonW, BUTTONS_HEIGHT);

        removeButton.setPosition(x + buttonW + SPACING, y + TITLE_HEIGHT + SPACING);
        removeButton.setSize(buttonW, BUTTONS_HEIGHT);

        list.setPosition(x, y + TITLE_HEIGHT + BUTTONS_HEIGHT + SPACING * 2);
        list.setSize(w, Math.max(listHeight, 20));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Titre
        graphics.drawString(Minecraft.getInstance().font, title,
                getX(), getY() + (TITLE_HEIGHT - 8) / 2, ModOptionsGui.TEXT_COLOR, false);

        addButton.render(graphics, mouseX, mouseY, partialTick);
        removeButton.render(graphics, mouseX, mouseY, partialTick);
        list.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (addButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (removeButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (list.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return list.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}