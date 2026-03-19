package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import io.github.xiaocihua.stacktonearbychests.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.xiaocihua.stacktonearbychests.ModOptions.MOD_ID;

/**
 * Interface d'options du mod.
 * Remplace ModOptionsGui (LibGui LightweightGuiDescription).
 * Géré par ModOptionsScreen.
 */
public class ModOptionsGui {

    public static final String PREFIX = MOD_ID + ".options.";
    public static final int TEXT_COLOR = 0xFF_F5F5F5;

    private static final ResourceLocation CHECKED = ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "textures/checkbox_checked.png");
    private static final ResourceLocation UNCHECKED = ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "textures/checkbox_unchecked.png");

    private static final int ROOT_WIDTH = 400;
    private static final int ROOT_HEIGHT = 240;
    private static final int PADDING = 8;
    private static final int SPACING = 6;

    private final ModOptions options = ModOptions.get();

    private WTabPanelCustom tabs;
    private FlatColorButton doneButton;

    @Nullable
    private EntryPicker currentDialog;

    private Screen screen;
    private int screenWidth;
    private int screenHeight;

    // ── Init ─────────────────────────────────────────────────────────────────────

    public void init(Screen screen, int width, int height) {
        this.screen = screen;
        this.screenWidth = width;
        this.screenHeight = height;

        int rootX = (width - ROOT_WIDTH) / 2;
        int rootY = (height - ROOT_HEIGHT) / 2;

        int tabContentH = ROOT_HEIGHT - 20 /* title */ - 16 /* tabs bar */ - 32 /* bottom */;

        tabs = new WTabPanelCustom(ROOT_WIDTH, tabContentH);
        tabs.setPosition(rootX, rootY + 20);
        tabs.add(buildScrollableTab(buildAppearance(rootX)),
                b -> b.title(Component.translatable(PREFIX + "appearance")));
        tabs.add(buildScrollableTab(buildBehavior(rootX)),
                b -> b.title(Component.translatable(PREFIX + "behavior")));
        tabs.add(buildScrollableTab(buildKeymap(rootX)),
                b -> b.title(Component.translatable(PREFIX + "keymap")));

        doneButton = new FlatColorButton(
                Component.translatable(PREFIX + "done"),
                btn -> {
                    options.write();
                    Minecraft.getInstance().screen.onClose();
                });
        doneButton.setBorder();
        doneButton.setPosition(rootX + (ROOT_WIDTH - 160) / 2,
                rootY + ROOT_HEIGHT - 32 + 6);
        doneButton.setSize(160, 20);
    }

    /** Enveloppe une liste de widgets dans un panneau scrollable vertical. */
    private ScrollablePanel buildScrollableTab(List<AbstractWidget> widgets) {
        return new ScrollablePanel(widgets, ROOT_WIDTH - PADDING * 2, Integer.MAX_VALUE);
    }

    // ── Onglets
    // ───────────────────────────────────────────────────────────────────

    private List<AbstractWidget> buildAppearance(int rootX) {
        List<AbstractWidget> list = new ArrayList<>();
        int w = ROOT_WIDTH - PADDING * 2;

        list.add(new FavoriteItemStyleButton(w));
        addCheckbox(list, "alwaysShowMarkersForFavoritedItems", options.appearance.alwaysShowMarkersForFavoritedItems,
                w);
        addCheckbox(list, "enableFavoritingSoundEffect", options.appearance.enableFavoritingSoundEffect, w);
        addCheckbox(list, "showStackToNearbyContainersButton", options.appearance.showStackToNearbyContainersButton, w);
        addCheckbox(list, "showRestockFromNearbyContainersButton",
                options.appearance.showRestockFromNearbyContainersButton, w);
        addCheckbox(list, "showQuickStackButton", options.appearance.showQuickStackButton, w);
        addCheckbox(list, "showRestockButton", options.appearance.showRestockButton, w);
        addCheckbox(list, "showTheButtonsOnTheCreativeInventoryScreen",
                options.appearance.showTheButtonsOnTheCreativeInventoryScreen, w);
        addCheckbox(list, "showButtonTooltip", options.appearance.showButtonTooltip, w);

        String posTip = PREFIX + "buttonPos.tooltip";
        addIntField(list, "stackToNearbyContainersButtonPosX", options.appearance.stackToNearbyContainersButtonPosX, w,
                null);
        addIntField(list, "stackToNearbyContainersButtonPosY", options.appearance.stackToNearbyContainersButtonPosY, w,
                null);
        addIntField(list, "restockFromNearbyContainersButtonPosX",
                options.appearance.restockFromNearbyContainersButtonPosX, w, null);
        addIntField(list, "restockFromNearbyContainersButtonPosY",
                options.appearance.restockFromNearbyContainersButtonPosY, w, null);
        addIntField(list, "quickStackButtonPosX", options.appearance.quickStackButtonPosX, w, posTip);
        addIntField(list, "quickStackButtonPosY", options.appearance.quickStackButtonPosY, w, posTip);
        addIntField(list, "restockButtonPosX", options.appearance.restockButtonPosX, w, posTip);
        addIntField(list, "restockButtonPosY", options.appearance.restockButtonPosY, w, posTip);
        return list;
    }

    private List<AbstractWidget> buildBehavior(int rootX) {
        List<AbstractWidget> list = new ArrayList<>();
        int w = ROOT_WIDTH - PADDING * 2;

        var searchInterval = new TextFieldWithLabel(
                Component.translatable(PREFIX + "searchInterval"), TEXT_COLOR,
                options.behavior.searchInterval::reset);
        searchInterval.getTextField().setValue(String.valueOf(options.behavior.searchInterval.intValue()));
        searchInterval.getTextField()
                .setResponder(t -> options.behavior.searchInterval.setValue(NumberUtils.toInt(t, 0)));
        searchInterval.withTooltip(PREFIX + "searchInterval.tooltip");
        list.add(searchInterval);

        addCheckbox(list, "supportForContainerEntities", options.behavior.supportForContainerEntities, w);
        addCheckbox(list, "doNotQuickStackItemsFromTheHotbar", options.behavior.doNotQuickStackItemsFromTheHotbar, w);
        addCheckbox(list, "enableItemFavoriting", options.behavior.enableItemFavoriting, w);
        addCheckbox(list, "favoriteItemsCannotBePickedUp", options.behavior.favoriteItemsCannotBePickedUp, w);
        addCheckbox(list, "favoriteItemStacksCannotBeThrown", options.behavior.favoriteItemStacksCannotBeThrown, w);
        addCheckbox(list, "favoriteItemStacksCannotBeQuickMoved", options.behavior.favoriteItemStacksCannotBeQuickMoved,
                w);
        addCheckbox(list, "favoriteItemStacksCannotBeSwapped", options.behavior.favoriteItemStacksCannotBeSwapped, w);
        addCheckbox(list, "favoriteItemsCannotBeSwappedWithOffhand",
                options.behavior.favoriteItemsCannotBeSwappedWithOffhand, w);

        list.add(new BlackWhiteList(Component.translatable(PREFIX + "stackingTargets"),
                options.behavior.stackingTargets, BlockContainerEntry::new,
                c -> openDialog(new EntryPicker.BlockContainerPicker(c)),
                data -> options.behavior.stackingTargets = data));
        list.add(new BlackWhiteList(Component.translatable(PREFIX + "stackingTargetEntities"),
                options.behavior.stackingTargetEntities, EntityContainerEntry::new,
                c -> openDialog(new EntryPicker.EntityContainerPicker(c)),
                data -> options.behavior.stackingTargetEntities = data));
        list.add(new BlackWhiteList(Component.translatable(PREFIX + "itemsThatWillNotBeStacked"),
                options.behavior.itemsThatWillNotBeStacked, ItemEntry::new,
                c -> openDialog(new EntryPicker.ItemPicker(c)),
                data -> options.behavior.itemsThatWillNotBeStacked = data));
        list.add(new BlackWhiteList(Component.translatable(PREFIX + "restockingSources"),
                options.behavior.restockingSources, BlockContainerEntry::new,
                c -> openDialog(new EntryPicker.BlockContainerPicker(c)),
                data -> options.behavior.restockingSources = data));
        list.add(new BlackWhiteList(Component.translatable(PREFIX + "restockingSourceEntities"),
                options.behavior.restockingSourceEntities, EntityContainerEntry::new,
                c -> openDialog(new EntryPicker.EntityContainerPicker(c)),
                data -> options.behavior.restockingSourceEntities = data));
        list.add(new BlackWhiteList(Component.translatable(PREFIX + "itemsThatWillNotBeRestocked"),
                options.behavior.itemsThatWillNotBeRestocked, ItemEntry::new,
                c -> openDialog(new EntryPicker.ItemPicker(c)),
                data -> options.behavior.itemsThatWillNotBeRestocked = data));
        return list;
    }

    private List<AbstractWidget> buildKeymap(int rootX) {
        List<AbstractWidget> list = new ArrayList<>();
        int w = ROOT_WIDTH - PADDING * 2;

        addKeymap(list, "stackToNearbyContainers", options.keymap.stackToNearbyContainersKey, w);
        addKeymap(list, "quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainers",
                options.keymap.quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey, w);
        addKeymap(list, "restockFromNearbyContainers", options.keymap.restockFromNearbyContainersKey, w);
        addKeymap(list, "quickStack", options.keymap.quickStackKey, w);
        addKeymap(list, "restock", options.keymap.restockKey, w);
        addKeymap(list, "markAsFavorite", options.keymap.markAsFavoriteKey, w);
        addKeymap(list, "showMarkersForFavoritedItems", options.keymap.showMarkersForFavoritedItemsKey, w);
        addKeymap(list, "openModOptionsScreen", options.keymap.openModOptionsScreenKey, w);

        list.add(new HintLabel(Component.translatable(PREFIX + "keyMapHint")
                .withStyle(Style.EMPTY.withItalic(true)), 0xFF_BFBFBF, w));
        return list;
    }

    // ── Helpers
    // ───────────────────────────────────────────────────────────────────

    private void addCheckbox(List<AbstractWidget> list, String key, MutableBoolean value, int w) {
        SimpleCheckbox sc = new SimpleCheckbox(Component.translatable(PREFIX + key), value, w);
        list.add(new SimpleCheckbox(Component.translatable(PREFIX + key), value, w));
    }

    private void addIntField(List<AbstractWidget> list, String key,
            ModOptions.IntOption value, int w, @Nullable String tooltipKey) {
        var field = new TextFieldWithLabel(
                Component.translatable(PREFIX + key), TEXT_COLOR, value::reset);
        field.getTextField().setValue(String.valueOf(value.intValue()));
        field.getTextField().setResponder(t -> {
            if (t.matches("-?\\d+"))
                value.setValue(NumberUtils.toInt(t));
        });
        if (tooltipKey != null)
            field.withTooltip(tooltipKey);
        list.add(field);
    }

    private void addKeymap(List<AbstractWidget> list, String key,
            io.github.xiaocihua.stacktonearbychests.KeySequence seq, int w) {
        var entry = new KeymapEntry(Component.translatable(PREFIX + key), seq);
        entry.setWidth(w);
        entry.layout();
        list.add(entry);
    }

    // ── Rendu
    // ─────────────────────────────────────────────────────────────────────

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int rootX = (screenWidth - ROOT_WIDTH) / 2;
        int rootY = (screenHeight - ROOT_HEIGHT) / 2;

        graphics.fill(rootX, rootY, rootX + ROOT_WIDTH, rootY + ROOT_HEIGHT, 0xFF_1E1E1E);
        drawBorder(graphics, rootX, rootY, ROOT_WIDTH, ROOT_HEIGHT);

        graphics.drawCenteredString(Minecraft.getInstance().font,
                Component.translatable(PREFIX + "title"),
                rootX + ROOT_WIDTH / 2, rootY + 6, TEXT_COLOR);

        tabs.render(graphics, mouseX, mouseY, partialTick);
        doneButton.render(graphics, mouseX, mouseY, partialTick);

        if (currentDialog != null) {
            graphics.fill(rootX, rootY, rootX + ROOT_WIDTH, rootY + ROOT_HEIGHT, 0x88_000000);
            currentDialog.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h) {
        int c = 0xFF_444444;
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    // ── Événements
    // ────────────────────────────────────────────────────────────────

    public boolean mouseClicked(double mx, double my, int btn) {
        if (currentDialog != null)
            return currentDialog.mouseClicked(mx, my, btn);
        if (tabs.mouseClicked(mx, my, btn))
            return true;
        return doneButton.mouseClicked(mx, my, btn);
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (currentDialog != null)
            return currentDialog.mouseScrolled(mx, my, sx, sy);
        return tabs.mouseScrolled(mx, my, sx, sy);
    }

    public boolean keyPressed(int keyCode, int scan, int mods) {
        if (currentDialog != null)
            return currentDialog.keyPressed(keyCode, scan, mods);
        return tabs.keyPressed(keyCode, scan, mods);
    }

    public boolean charTyped(char c, int mods) {
        if (currentDialog != null)
            return currentDialog.charTyped(c, mods);
        return tabs.charTyped(c, mods);
    }

    public void onClose() {
        options.write();
    }

    // ── Dialog
    // ────────────────────────────────────────────────────────────────────

    public void openDialog(EntryPicker dialog) {
        if (currentDialog != null)
            return;
        currentDialog = dialog;
        int rootX = (screenWidth - ROOT_WIDTH) / 2;
        int rootY = (screenHeight - ROOT_HEIGHT) / 2;
        dialog.setPosition(rootX + (ROOT_WIDTH - dialog.getWidth()) / 2,
                rootY + (ROOT_HEIGHT - dialog.getHeight()) / 2);
        dialog.layout();
        dialog.setOnClose(() -> currentDialog = null);
    }

    // ── Widgets internes
    // ──────────────────────────────────────────────────────────

    /** Panneau scrollable vertical contenant une liste de widgets. */
    private static class ScrollablePanel extends AbstractWidget {
        private final List<AbstractWidget> widgets;
        private int scrollOffset = 0;
        private static final int ROW_H = 22;
        private static final int SCROLL_W = 8;

        ScrollablePanel(List<AbstractWidget> widgets, int w, int h) {
            super(0, 0, w, h, Component.empty());
            this.widgets = widgets;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            int usableH = height;
            graphics.enableScissor(x, y, x + width, y + usableH);

            int currentY = y - scrollOffset;
            for (AbstractWidget w : widgets) {
                int wH = Math.max(w.getHeight(), ROW_H);
                w.setPosition(x + PADDING, currentY + (wH - w.getHeight()) / 2);
                w.setWidth(width - PADDING * 2 - SCROLL_W);
                if (w instanceof TextFieldWithLabel tf)
                    tf.layout();
                if (w instanceof KeymapEntry ke)
                    ke.layout();
                if (w instanceof BlackWhiteList bwl)
                    bwl.layout();
                w.render(graphics, mouseX, mouseY, partialTick);
                currentY += wH + SPACING;
            }

            graphics.disableScissor();
            drawScrollbar(graphics, x + width - SCROLL_W, y, usableH, currentY - y + scrollOffset);
        }

        private void drawScrollbar(GuiGraphics g, int x, int y, int h, int totalH) {
            g.fill(x, y, x + SCROLL_W, y + h, 0xFF_1A1A1A);
            if (totalH <= h)
                return;
            float ratio = (float) h / totalH;
            int handleH = Math.max(6, (int) (h * ratio));
            float sRatio = (float) scrollOffset / (totalH - h);
            int handleY = y + (int) ((h - handleH) * sRatio);
            g.fill(x + 1, handleY, x + SCROLL_W - 1, handleY + handleH, 0xFF_515151);
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double sx, double sy) {
            if (!isMouseOver(mx, my))
                return false;
            int totalH = widgets.stream().mapToInt(w -> Math.max(w.getHeight(), ROW_H) + SPACING).sum();
            scrollOffset = Mth.clamp((int) (scrollOffset - sy * 10), 0, Math.max(0, totalH - height));
            return true;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn) {
            for (AbstractWidget w : widgets) {
                if (w.mouseClicked(mx, my, btn))
                    return true;
            }
            return false;
        }

        @Override
        public boolean keyPressed(int k, int s, int m) {
            for (AbstractWidget w : widgets) {
                if (w.keyPressed(k, s, m))
                    return true;
            }
            return false;
        }

        @Override
        public boolean charTyped(char c, int m) {
            for (AbstractWidget w : widgets) {
                if (w.charTyped(c, m))
                    return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput o) {
        }
    }

    /** Checkbox simple liée à un MutableBoolean. */
    private static class SimpleCheckbox extends AbstractWidget {
        private final MutableBoolean value;
        private final Component label;

        SimpleCheckbox(Component label, MutableBoolean value, int w) {
            super(0, 0, w, 20, label);
            this.value = value;
            this.label = label;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = isHovered();
            boolean checked = value.booleanValue();

            // Dessin de la case
            int boxSize = 12;
            int boxX = getX();
            int boxY = getY() + (height - boxSize) / 2;

            graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF000000); // bordure noire
            graphics.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1,
                    checked ? 0xFF00FF00 : 0xFFFFFFFF); // vert si coché, blanc sinon

            // Label
            graphics.drawString(Minecraft.getInstance().font,
                    label, boxX + boxSize + 4, getY() + 6, 0xFFFFFF);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            value.setValue(!value.booleanValue());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            // Rien à narrer pour ce widget custom
        }
    }

    /** Bouton cyclique pour le style des favoris. */
    private class FavoriteItemStyleButton extends FlatColorButton {
        private int index;

        FavoriteItemStyleButton(int w) {
            super(Component.translatable(PREFIX + "favoriteItemStyle"), btn -> {
            });
            index = Math.max(0, LockedSlots.FAVORITE_ITEM_TAGS.indexOf(options.appearance.favoriteItemStyle));
            setSize(w, 20);
            setBorder();
            refreshLabel();
        }

        @Override
        public void onPress() {
            boolean shift = Screen.hasShiftDown();
            index = Mth.positiveModulo(index + (shift ? -1 : 1), LockedSlots.FAVORITE_ITEM_TAGS.size());
            options.appearance.favoriteItemStyle = LockedSlots.FAVORITE_ITEM_TAGS.get(index);
            refreshLabel();
        }

        private void refreshLabel() {
            ResourceLocation id = LockedSlots.FAVORITE_ITEM_TAGS.get(index);
            setMessage(Component.translatable(MOD_ID + ".resource." + id.getPath()));
        }
    }

    /** Label italique pour les hints. */
    private static class HintLabel extends AbstractWidget {
        private final Component text;
        private final int color;

        HintLabel(Component text, int color, int w) {
            super(0, 0, w, 12, text);
            this.text = text;
            this.color = color;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mx, int my, float pt) {
            graphics.drawString(Minecraft.getInstance().font, text, getX(), getY() + 2, color, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput o) {
        }
    }
}