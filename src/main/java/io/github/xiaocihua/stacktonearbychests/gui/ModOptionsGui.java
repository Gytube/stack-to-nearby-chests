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

public class ModOptionsGui {

    public static final String PREFIX     = MOD_ID + ".options.";
    public static final int    TEXT_COLOR = 0xFF_F5F5F5;

    private static final int ROOT_WIDTH  = 400;
    private static final int ROOT_HEIGHT = 240;
    private static final int TAB_HEIGHT  = 16;

    private final ModOptions options = ModOptions.get();
    private final List<AbstractWidget> widgets = new ArrayList<>();

    private WTabPanelCustom tabs;

    @Nullable private EntryPicker currentDialog;

    private int rootX;
    private int rootY;

    public void init(ModOptionsScreen screen, int screenWidth, int screenHeight) {
        widgets.clear();
        rootX = (screenWidth  - ROOT_WIDTH)  / 2;
        rootY = (screenHeight - ROOT_HEIGHT) / 2;

        int tabContentH = ROOT_HEIGHT - 20 - 32 - TAB_HEIGHT;

        tabs = new WTabPanelCustom(ROOT_WIDTH, ROOT_HEIGHT - 20 - 32);
        tabs.setPosition(rootX, rootY + 20);
        tabs.add(createAppearancePanel(tabContentH), b -> b.title(Component.translatable(PREFIX + "appearance")));
        tabs.add(createBehaviorPanel(tabContentH),   b -> b.title(Component.translatable(PREFIX + "behavior")));
        tabs.add(createKeymapPanel(tabContentH),     b -> b.title(Component.translatable(PREFIX + "keymap")));
        widgets.add(tabs);

        FlatColorButton doneButton = new FlatColorButton(
                Component.translatable(PREFIX + "done"),
                btn -> { options.write(); screen.onClose(); });
        doneButton.setBorder();
        doneButton.setPosition(rootX + (ROOT_WIDTH - 160) / 2, rootY + ROOT_HEIGHT - 28);
        doneButton.setSize(160, 20);
        widgets.add(doneButton);
    }

    private ScrollablePanel createAppearancePanel(int h) {
        var p = new ScrollablePanel(ROOT_WIDTH - 8, h);
        p.addWidget(createFavoriteStyleButton());
        p.addWidget(createCheckbox("alwaysShowMarkersForFavoritedItems",    options.appearance.alwaysShowMarkersForFavoritedItems));
        p.addWidget(createCheckbox("enableFavoritingSoundEffect",           options.appearance.enableFavoritingSoundEffect));
        p.addWidget(createCheckbox("showStackToNearbyContainersButton",     options.appearance.showStackToNearbyContainersButton));
        p.addWidget(createCheckbox("showRestockFromNearbyContainersButton", options.appearance.showRestockFromNearbyContainersButton));
        p.addWidget(createCheckbox("showQuickStackButton",                  options.appearance.showQuickStackButton));
        p.addWidget(createCheckbox("showRestockButton",                     options.appearance.showRestockButton));
        p.addWidget(createCheckbox("showTheButtonsOnTheCreativeInventoryScreen", options.appearance.showTheButtonsOnTheCreativeInventoryScreen));
        p.addWidget(createCheckbox("showButtonTooltip",                     options.appearance.showButtonTooltip));
        String posTooltip = PREFIX + "buttonPos.tooltip";
        p.addWidget(createIntTextField("stackToNearbyContainersButtonPosX",    options.appearance.stackToNearbyContainersButtonPosX));
        p.addWidget(createIntTextField("stackToNearbyContainersButtonPosY",    options.appearance.stackToNearbyContainersButtonPosY));
        p.addWidget(createIntTextField("restockFromNearbyContainersButtonPosX",options.appearance.restockFromNearbyContainersButtonPosX));
        p.addWidget(createIntTextField("restockFromNearbyContainersButtonPosY",options.appearance.restockFromNearbyContainersButtonPosY));
        p.addWidget(createIntTextField("quickStackButtonPosX", options.appearance.quickStackButtonPosX).withTooltip(posTooltip));
        p.addWidget(createIntTextField("quickStackButtonPosY", options.appearance.quickStackButtonPosY).withTooltip(posTooltip));
        p.addWidget(createIntTextField("restockButtonPosX",    options.appearance.restockButtonPosX).withTooltip(posTooltip));
        p.addWidget(createIntTextField("restockButtonPosY",    options.appearance.restockButtonPosY).withTooltip(posTooltip));
        return p;
    }

    private ScrollablePanel createBehaviorPanel(int h) {
        var p = new ScrollablePanel(ROOT_WIDTH - 8, h);
        var si = createIntTextField("searchInterval", options.behavior.searchInterval)
                .withTooltip(PREFIX + "searchInterval.tooltip");
        si.getTextField().setFilter(text -> NumberUtils.toInt(text, -1) >= 0);
        p.addWidget(si);
        p.addWidget(createCheckbox("supportForContainerEntities",             options.behavior.supportForContainerEntities));
        p.addWidget(createCheckbox("doNotQuickStackItemsFromTheHotbar",       options.behavior.doNotQuickStackItemsFromTheHotbar));
        p.addWidget(createCheckbox("enableItemFavoriting",                    options.behavior.enableItemFavoriting));
        p.addWidget(createCheckbox("favoriteItemsCannotBePickedUp",           options.behavior.favoriteItemsCannotBePickedUp));
        p.addWidget(createCheckbox("favoriteItemStacksCannotBeThrown",        options.behavior.favoriteItemStacksCannotBeThrown));
        p.addWidget(createCheckbox("favoriteItemStacksCannotBeQuickMoved",    options.behavior.favoriteItemStacksCannotBeQuickMoved));
        p.addWidget(createCheckbox("favoriteItemStacksCannotBeSwapped",       options.behavior.favoriteItemStacksCannotBeSwapped));
        p.addWidget(createCheckbox("favoriteItemsCannotBeSwappedWithOffhand", options.behavior.favoriteItemsCannotBeSwappedWithOffhand));
        p.addWidget(bwl(PREFIX + "stackingTargets",         options.behavior.stackingTargets,         BlockContainerEntry::new, EntryPicker.BlockContainerPicker::new, d -> options.behavior.stackingTargets = d));
        p.addWidget(bwl(PREFIX + "stackingTargetEntities",  options.behavior.stackingTargetEntities,  EntityContainerEntry::new, EntryPicker.EntityContainerPicker::new, d -> options.behavior.stackingTargetEntities = d));
        p.addWidget(bwl(PREFIX + "itemsThatWillNotBeStacked",options.behavior.itemsThatWillNotBeStacked, ItemEntry::new, EntryPicker.ItemPicker::new, d -> options.behavior.itemsThatWillNotBeStacked = d));
        p.addWidget(bwl(PREFIX + "restockingSources",       options.behavior.restockingSources,       BlockContainerEntry::new, EntryPicker.BlockContainerPicker::new, d -> options.behavior.restockingSources = d));
        p.addWidget(bwl(PREFIX + "restockingSourceEntities",options.behavior.restockingSourceEntities,EntityContainerEntry::new, EntryPicker.EntityContainerPicker::new, d -> options.behavior.restockingSourceEntities = d));
        p.addWidget(bwl(PREFIX + "itemsThatWillNotBeRestocked",options.behavior.itemsThatWillNotBeRestocked, ItemEntry::new, EntryPicker.ItemPicker::new, d -> options.behavior.itemsThatWillNotBeRestocked = d));
        return p;
    }

    private <E extends SelectableEntryList.Entry<ResourceLocation>> BlackWhiteList bwl(
            String labelKey,
            java.util.Set<String> data,
            java.util.function.Function<ResourceLocation, E> entryFn,
            java.util.function.Function<java.util.function.Consumer<java.util.List<ResourceLocation>>, EntryPicker> pickerFn,
            java.util.function.Consumer<java.util.Set<String>> setter) {
        return new BlackWhiteList(Component.translatable(labelKey), data,
                (java.util.function.Function) entryFn,
                c -> openDialog(pickerFn.apply(c)),
                setter);
    }

    private ScrollablePanel createKeymapPanel(int h) {
        var p = new ScrollablePanel(ROOT_WIDTH - 8, h);
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "stackToNearbyContainers"), options.keymap.stackToNearbyContainersKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainers"), options.keymap.quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "restockFromNearbyContainers"), options.keymap.restockFromNearbyContainersKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "quickStack"),                  options.keymap.quickStackKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "restock"),                     options.keymap.restockKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "markAsFavorite"),              options.keymap.markAsFavoriteKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "showMarkersForFavoritedItems"),options.keymap.showMarkersForFavoritedItemsKey));
        p.addWidget(new KeymapEntry(Component.translatable(PREFIX + "openModOptionsScreen"),        options.keymap.openModOptionsScreenKey));
        p.addLabel(Component.translatable(PREFIX + "keyMapHint").setStyle(Style.EMPTY.withItalic(true)), 0xFF_BFBFBF);
        return p;
    }

    private AbstractWidget createFavoriteStyleButton() {
        int[] index = { LockedSlots.FAVORITE_ITEM_TAGS.indexOf(options.appearance.favoriteItemStyle) };
        FlatColorButton btn = new FlatColorButton(
                Component.translatable(MOD_ID + ".resource." + options.appearance.favoriteItemStyle.getPath()),
                b -> {
                    // ✅ fix: Screen.hasShiftDown() méthode statique
                    boolean shift = Screen.hasShiftDown();
                    index[0] = Mth.positiveModulo(index[0] + (shift ? -1 : 1),
                            LockedSlots.FAVORITE_ITEM_TAGS.size());
                    ResourceLocation id = LockedSlots.FAVORITE_ITEM_TAGS.get(index[0]);
                    options.appearance.favoriteItemStyle = id;
                    b.setMessage(Component.translatable(MOD_ID + ".resource." + id.getPath()));
                }) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                graphics.blit(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/slot_background.png"),
                        getX() + 1, getY() + 1, 0, 0, 18, 18, 18, 18);
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
        };
        btn.setBorder();
        return btn;
    }

    private Checkbox createCheckbox(String key, MutableBoolean value) {
        // ✅ fix: Checkbox.builder() API correcte pour 1.21.1
        return Checkbox.builder(Component.translatable(PREFIX + key), Minecraft.getInstance().font)
                .selected(value.booleanValue())
                .onValueChange((cb, v) -> value.setValue(v))
                .build();
    }

    private TextFieldWithLabel createIntTextField(String key, ModOptions.IntOption value) {
        var field = new TextFieldWithLabel(Component.translatable(PREFIX + key), TEXT_COLOR, value::reset);
        field.getTextField().setValue(String.valueOf(value.intValue()));
        field.getTextField().setResponder(text -> value.setValue(NumberUtils.toInt(text)));
        field.getTextField().setFilter(text -> text.matches("-?\\d*"));
        return field;
    }

    public void openDialog(EntryPicker dialog) {
        if (currentDialog != null) return;
        currentDialog = dialog;
        // ✅ fix: utiliser getWidth()/getHeight() au lieu des champs protégés
        dialog.setPosition(
                rootX + (ROOT_WIDTH  - dialog.getWidth())  / 2,
                rootY + (ROOT_HEIGHT - dialog.getHeight()) / 2);
        dialog.setOnClose(() -> {
            currentDialog = null;
            widgets.remove(dialog);
        });
        dialog.layout();
        widgets.add(dialog);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(rootX, rootY, rootX + ROOT_WIDTH, rootY + ROOT_HEIGHT, 0xFF_2B2B2B);
        drawBorder(graphics, rootX, rootY, ROOT_WIDTH, ROOT_HEIGHT);
        graphics.drawCenteredString(Minecraft.getInstance().font,
                Component.translatable(PREFIX + "title"),
                rootX + ROOT_WIDTH / 2, rootY + 6, TEXT_COLOR);
        for (AbstractWidget w : widgets) {
            if (w == currentDialog) continue;
            w.render(graphics, mouseX, mouseY, partialTick);
        }
        if (currentDialog != null) {
            graphics.fill(rootX, rootY, rootX + ROOT_WIDTH, rootY + ROOT_HEIGHT, 0x88_000000);
            currentDialog.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (currentDialog != null) return currentDialog.mouseClicked(mx, my, button);
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseClicked(mx, my, button)) return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (currentDialog != null) return currentDialog.mouseScrolled(mx, my, sx, sy);
        for (AbstractWidget w : widgets) { if (w.mouseScrolled(mx, my, sx, sy)) return true; }
        return false;
    }

    public boolean keyPressed(int k, int s, int m) {
        if (currentDialog != null) return currentDialog.keyPressed(k, s, m);
        for (AbstractWidget w : widgets) { if (w.keyPressed(k, s, m)) return true; }
        return false;
    }

    public boolean charTyped(char c, int m) {
        if (currentDialog != null) return currentDialog.charTyped(c, m);
        for (AbstractWidget w : widgets) { if (w.charTyped(c, m)) return true; }
        return false;
    }

    public void onClose() { options.write(); }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h) {
        int c = 0xFF_555555;
        g.fill(x, y, x+w, y+1, c); g.fill(x, y+h-1, x+w, y+h, c);
        g.fill(x, y, x+1, y+h, c); g.fill(x+w-1, y, x+w, y+h, c);
    }

    // ── ScrollablePanel ───────────────────────────────────────────────────────────

    public static class ScrollablePanel extends AbstractWidget {
        private static final int SCROLLBAR_W  = 8;
        private static final int ITEM_SPACING = 4;
        private final List<AbstractWidget> items = new ArrayList<>();
        private int scrollOffset = 0;

        public ScrollablePanel(int width, int height) {
            super(0, 0, width, height, Component.empty());
        }

        public void addWidget(AbstractWidget w) { items.add(w); }

        public void addLabel(Component text, int color) {
            items.add(new LabelWidget(text, color));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX(); int y = getY();
            int contentW = width - SCROLLBAR_W;
            graphics.enableScissor(x, y, x + width, y + height);
            int curY = y - scrollOffset;
            for (AbstractWidget w : items) {
                w.setPosition(x, curY);
                w.setWidth(contentW);
                if (w instanceof TextFieldWithLabel tf) tf.layout();
                if (w instanceof KeymapEntry ke)        ke.layout();
                if (w instanceof BlackWhiteList bwl)    bwl.layout();
                if (curY + w.getHeight() > y && curY < y + height) {
                    w.render(graphics, mouseX, mouseY, partialTick);
                }
                curY += w.getHeight() + ITEM_SPACING;
            }
            graphics.disableScissor();
            int totalH = totalHeight();
            if (totalH > height) {
                graphics.fill(x + contentW, y, x + width, y + height, 0xFF_262626);
                int handleH = Math.max(6, height * height / totalH);
                int handleY = y + (height - handleH) * scrollOffset / Math.max(1, totalH - height);
                graphics.fill(x + contentW + 1, handleY, x + width - 1, handleY + handleH, 0xFF_515151);
            }
        }

        private int totalHeight() {
            return items.stream().mapToInt(w -> w.getHeight() + ITEM_SPACING).sum();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!isMouseOver(mx, my)) return false;
            for (AbstractWidget w : items) { if (w.mouseClicked(mx, my, button)) return true; }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double sx, double sy) {
            if (!isMouseOver(mx, my)) return false;
            int totalH = totalHeight();
            if (totalH > height) scrollOffset = Mth.clamp((int)(scrollOffset - sy * 10), 0, totalH - height);
            return true;
        }

        @Override
        public boolean keyPressed(int k, int s, int m) {
            for (AbstractWidget w : items) { if (w.isFocused() && w.keyPressed(k, s, m)) return true; }
            return false;
        }

        @Override
        public boolean charTyped(char c, int m) {
            for (AbstractWidget w : items) { if (w.isFocused() && w.charTyped(c, m)) return true; }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput o) { defaultButtonNarrationText(o); }

        private static class LabelWidget extends AbstractWidget {
            private final Component text; private final int color;
            LabelWidget(Component t, int c) { super(0, 0, 0, 10, t); text = t; color = c; }
            @Override public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
                g.drawString(Minecraft.getInstance().font, text, getX(), getY(), color, false);
            }
            @Override protected void updateWidgetNarration(NarrationElementOutput o) {}
        }
    }
}