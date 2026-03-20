package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance; // ✅ fix
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class WTabPanelCustom extends AbstractWidget {

    private static final int TAB_HEIGHT = 16;

    private final List<TabEntry> tabs = new ArrayList<>();
    private int selectedTab = 0;

    private final int cardWidth;
    private final int cardHeight;

    public WTabPanelCustom(int cardWidth, int cardHeight) {
        super(0, 0, cardWidth, cardHeight + TAB_HEIGHT, Component.empty());
        this.cardWidth  = cardWidth;
        this.cardHeight = cardHeight;
    }

    public void add(AbstractWidget widget, Consumer<Tab.Builder> configurator) {
        Tab.Builder builder = new Tab.Builder(widget);
        configurator.accept(builder);
        tabs.add(new TabEntry(builder.build()));
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
            int tabW = 28 + (entry.tab.title != null ? font.width(entry.tab.title) : 0);

            graphics.fill(tabX, y, tabX + tabW, y + TAB_HEIGHT, selected ? 0xFF_3C3F41 : 0xFF_262626);

            if (entry.tab.title != null) {
                int textY = y + (TAB_HEIGHT - font.lineHeight) / 2;
                graphics.drawString(font, entry.tab.title, tabX + 4, textY, ModOptionsGui.TEXT_COLOR, false);
            }

            entry.tabX = tabX;
            entry.tabW = tabW;
            tabX += tabW + 1;
        }

        // ── Fond panneau ──
        int panelY = y + TAB_HEIGHT;
        graphics.fill(x, panelY, x + cardWidth, panelY + cardHeight, 0xFF_3C3F41);

        // ── Contenu ──
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
        for (int i = 0; i < tabs.size(); i++) {
            TabEntry entry = tabs.get(i);
            if (mouseX >= entry.tabX && mouseX < entry.tabX + entry.tabW
                    && mouseY >= y && mouseY < y + TAB_HEIGHT) {
                selectedTab = i;
                // ✅ fix: import correct
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        if (selectedTab < tabs.size()) {
            return tabs.get(selectedTab).tab.widget.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (selectedTab < tabs.size()) return tabs.get(selectedTab).tab.widget.mouseScrolled(mx, my, sx, sy);
        return false;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (selectedTab < tabs.size()) return tabs.get(selectedTab).tab.widget.keyPressed(k, s, m);
        return false;
    }

    @Override
    public boolean charTyped(char c, int m) {
        if (selectedTab < tabs.size()) return tabs.get(selectedTab).tab.widget.charTyped(c, m);
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private static class TabEntry {
        final Tab tab;
        int tabX, tabW;
        TabEntry(Tab tab) { this.tab = tab; }
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

            public Builder(AbstractWidget widget) { this.widget = widget; }
            public Builder title(Component t) { this.title = t; return this; }
            public Tab build() { return new Tab(title, widget); }
        }
    }
}