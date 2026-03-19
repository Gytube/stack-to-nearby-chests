package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Panneau à onglets vanilla.
 * Remplace WTabPanelCustom (LibGui WTabPanel).
 */
public class WTabPanelCustom extends AbstractWidget {

    private static final int TAB_HEIGHT = 16;
    private static final int TAB_WIDTH  = 28;

    private final List<TabEntry> tabs = new ArrayList<>();
    private int selectedTab = 0;

    private int cardWidth;
    private int cardHeight;

    public WTabPanelCustom(int cardWidth, int cardHeight) {
        super(0, 0, cardWidth, cardHeight + TAB_HEIGHT, Component.empty());
        this.cardWidth  = cardWidth;
        this.cardHeight = cardHeight;
    }

    public void add(AbstractWidget widget, Consumer<Tab.Builder> configurator) {
        Tab.Builder builder = new Tab.Builder(widget);
        configurator.accept(builder);
        Tab tab = builder.build();
        tabs.add(new TabEntry(tab, tabs.isEmpty()));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        var font = Minecraft.getInstance().font;

        // ── Onglets ──
        int tabX = x;
        for (int i = 0; i < tabs.size(); i++) {
            TabEntry entry = tabs.get(i);
            boolean selected = (i == selectedTab);
            int tabW = TAB_WIDTH + (entry.tab.title != null ? font.width(entry.tab.title) : 0);

            // Fond onglet
            int bg = selected ? 0xFF_3C3F41 : 0xFF_262626;
            graphics.fill(tabX, y, tabX + tabW, y + TAB_HEIGHT, bg);
            // Bordure basse si sélectionné
            if (selected) {
                graphics.fill(tabX, y + TAB_HEIGHT - 1, tabX + tabW, y + TAB_HEIGHT, 0xFF_3C3F41);
            }

            // Titre
            if (entry.tab.title != null) {
                int textY = y + (TAB_HEIGHT - font.lineHeight) / 2;
                graphics.drawString(font, entry.tab.title, tabX + 4, textY, ModOptionsGui.TEXT_COLOR, false);
            }

            entry.tabX = tabX;
            entry.tabW = tabW;
            tabX += tabW + 1;
        }

        // ── Fond du panneau principal ──
        int panelY = y + TAB_HEIGHT;
        graphics.fill(x, panelY, x + cardWidth, panelY + cardHeight, 0xFF_3C3F41);

        // ── Contenu de l'onglet sélectionné ──
        if (selectedTab < tabs.size()) {
            AbstractWidget content = tabs.get(selectedTab).tab.widget;
            content.setPosition(x + 4, panelY + 4);
            content.setWidth(cardWidth - 8);
            content.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = getY();
        // Clic sur un onglet
        for (int i = 0; i < tabs.size(); i++) {
            TabEntry entry = tabs.get(i);
            if (mouseX >= entry.tabX && mouseX < entry.tabX + entry.tabW
                    && mouseY >= y && mouseY < y + TAB_HEIGHT) {
                selectedTab = i;
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        // Clic dans le contenu
        if (selectedTab < tabs.size()) {
            return tabs.get(selectedTab).tab.widget.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectedTab < tabs.size()) {
            return tabs.get(selectedTab).tab.widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedTab < tabs.size()) {
            return tabs.get(selectedTab).tab.widget.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (selectedTab < tabs.size()) {
            return tabs.get(selectedTab).tab.widget.charTyped(c, modifiers);
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    // ── Données ──────────────────────────────────────────────────────────────────

    private static class TabEntry {
        final Tab tab;
        boolean selected;
        int tabX;
        int tabW;

        TabEntry(Tab tab, boolean selected) {
            this.tab = tab;
            this.selected = selected;
        }
    }

    public static class Tab {
        @Nullable final Component title;
        final AbstractWidget widget;

        private Tab(@Nullable Component title, AbstractWidget widget) {
            this.title  = title;
            this.widget = Objects.requireNonNull(widget);
        }

        public static class Builder {
            private final AbstractWidget widget;
            @Nullable private Component title;

            public Builder(AbstractWidget widget) {
                this.widget = widget;
            }

            public Builder title(Component title) {
                this.title = title; return this;
            }

            public Tab build() {
                return new Tab(title, widget);
            }
        }
    }
}