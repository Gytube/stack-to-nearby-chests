package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static io.github.xiaocihua.stacktonearbychests.ModOptions.MOD_ID;

/**
 * Panneau de sélection d'entrées (items, blocs, entités).
 * Remplace EntryPicker (LibGui WBox).
 */
public abstract class EntryPicker extends AbstractWidget {

    protected static final String PREFIX = ModOptionsGui.PREFIX + "entryPicker.";

    private static final int PADDING    = 4;
    private static final int FIELD_H    = 20;
    private static final int TITLE_H    = 12;
    private static final int BUTTON_H   = 20;
    private static final int BUTTON_W   = 50;
    private static final int LIST_H     = 140;

    protected final EditBox searchByName;
    protected final EditBox searchByID;
    protected SelectableEntryList<ResourceLocation> entryList;

    private final FlatColorButton addButton;
    private final FlatColorButton cancelButton;

    private Optional<Runnable> onClose = Optional.empty();

    public EntryPicker(Consumer<List<ResourceLocation>> consumer) {
        super(0, 0, 250, TITLE_H + FIELD_H * 2 + LIST_H + BUTTON_H + PADDING * 4, Component.empty());

        var font = Minecraft.getInstance().font;

        searchByName = new EditBox(font, 0, 0, width - PADDING * 2, FIELD_H,
                Component.translatable(PREFIX + "searchByName"));
        searchByName.setResponder(s -> entryList.setData(searchByName(s)));

        searchByID = new EditBox(font, 0, 0, width - PADDING * 2, FIELD_H,
                Component.translatable(PREFIX + "searchByID"));
        searchByID.setResponder(s -> entryList.setData(searchByID(s)));

        entryList = getEntryList();

        addButton = new FlatColorButton(
                Component.translatable(PREFIX + "add"),
                btn -> { consumer.accept(entryList.getSelectedData()); close(); });
        addButton.setBorder();

        cancelButton = new FlatColorButton(
                Component.translatable(PREFIX + "cancel"),
                btn -> close());
        cancelButton.setBorder();
    }

    /** Positionne les sous-widgets. À appeler après setPosition. */
    public void layout() {
        int x = getX() + PADDING;
        int y = getY() + PADDING;
        int w = width - PADDING * 2;

        searchByName.setPosition(x, y + TITLE_H + PADDING);
        searchByName.setWidth(w);

        searchByID.setPosition(x, y + TITLE_H + FIELD_H + PADDING * 2);
        searchByID.setWidth(w);

        entryList.setPosition(x, y + TITLE_H + FIELD_H * 2 + PADDING * 3);
        entryList.setSize(w, LIST_H);

        int buttonY = y + TITLE_H + FIELD_H * 2 + LIST_H + PADDING * 4;
        cancelButton.setPosition(x + w - BUTTON_W, buttonY);
        cancelButton.setSize(BUTTON_W, BUTTON_H);

        addButton.setPosition(x + w - BUTTON_W * 2 - PADDING, buttonY);
        addButton.setSize(BUTTON_W, BUTTON_H);
    }

    public abstract Component getTitle();
    public abstract List<ResourceLocation> searchByName(String searchStr);
    public abstract List<ResourceLocation> searchByID(String searchStr);
    public abstract SelectableEntryList<ResourceLocation> getEntryList();

    public void setOnClose(Runnable onClose) {
        this.onClose = Optional.ofNullable(onClose);
    }

    public void close() {
        onClose.ifPresent(Runnable::run);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fond avec bordure
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF_1A1A1A);
        drawBorder(graphics);

        // Titre
        graphics.drawString(Minecraft.getInstance().font, getTitle(),
                getX() + PADDING, getY() + PADDING, ModOptionsGui.TEXT_COLOR, false);

        searchByName.render(graphics, mouseX, mouseY, partialTick);
        searchByID.render(graphics, mouseX, mouseY, partialTick);
        entryList.render(graphics, mouseX, mouseY, partialTick);
        addButton.render(graphics, mouseX, mouseY, partialTick);
        cancelButton.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBorder(GuiGraphics g) {
        int color = 0xFF_444444;
        int x = getX(); int y = getY(); int w = width; int h = height;
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchByName.mouseClicked(mouseX, mouseY, button)) return true;
        if (searchByID.mouseClicked(mouseX, mouseY, button)) return true;
        if (entryList.mouseClicked(mouseX, mouseY, button)) return true;
        if (addButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (cancelButton.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchByName.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (searchByID.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (searchByName.charTyped(c, modifiers)) return true;
        if (searchByID.charTyped(c, modifiers)) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return entryList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    // ── Sous-classes ─────────────────────────────────────────────────────────────

    public static class ItemPicker extends EntryPicker {
        public ItemPicker(Consumer<List<ResourceLocation>> consumer) {
            super(consumer);
            entryList.setData(searchByName(""));
            layout();
        }

        @Override public Component getTitle() {
            return Component.translatable(PREFIX + "addItemsToList");
        }

        @Override public List<ResourceLocation> searchByName(String s) {
            return BuiltInRegistries.ITEM.stream()
                    .filter(item -> StringUtils.containsIgnoreCase(item.getDescription().getString(), s))
                    .map(BuiltInRegistries.ITEM::getKey)
                    .toList();
        }

        @Override public List<ResourceLocation> searchByID(String s) {
            return BuiltInRegistries.ITEM.keySet().stream()
                    .filter(id -> StringUtils.containsIgnoreCase(id.toString(), s))
                    .toList();
        }

        @Override public SelectableEntryList<ResourceLocation> getEntryList() {
            return new SelectableEntryList<>(ItemEntry::new);
        }
    }

    public static class BlockContainerPicker extends EntryPicker {
        public BlockContainerPicker(Consumer<List<ResourceLocation>> consumer) {
            super(consumer);
            entryList.setData(searchByName(""));
            layout();
        }

        @Override public Component getTitle() {
            return Component.translatable(PREFIX + "addContainersToList");
        }

        @Override public List<ResourceLocation> searchByName(String s) {
            return BuiltInRegistries.BLOCK.stream()
                    .filter(b -> BuiltInRegistries.BLOCK_ENTITY_TYPE.stream()
                            .anyMatch(bet -> bet.isValid(b.defaultBlockState())))
                    .filter(b -> StringUtils.containsIgnoreCase(b.getName().getString(), s))
                    .map(b -> BuiltInRegistries.BLOCK.getKey(b))
                    .toList();
        }

        @Override public List<ResourceLocation> searchByID(String s) {
            return BuiltInRegistries.BLOCK.stream()
                    .filter(b -> BuiltInRegistries.BLOCK_ENTITY_TYPE.stream()
                            .anyMatch(bet -> bet.isValid(b.defaultBlockState())))
                    .map(b -> BuiltInRegistries.BLOCK.getKey(b))
                    .filter(id -> StringUtils.containsIgnoreCase(id.toString(), s))
                    .toList();
        }

        @Override public SelectableEntryList<ResourceLocation> getEntryList() {
            return new SelectableEntryList<>(BlockContainerEntry::new);
        }
    }

    public static class EntityContainerPicker extends EntryPicker {
        public EntityContainerPicker(Consumer<List<ResourceLocation>> consumer) {
            super(consumer);
            entryList.setData(searchByName(""));
            layout();
        }

        @Override public Component getTitle() {
            return Component.translatable(PREFIX + "addContainersToList");
        }

        @Override public List<ResourceLocation> searchByName(String s) {
            return BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(et -> StringUtils.containsIgnoreCase(et.getDescription().getString(), s))
                    .map(et -> BuiltInRegistries.ENTITY_TYPE.getKey(et))
                    .toList();
        }

        @Override public List<ResourceLocation> searchByID(String s) {
            return BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                    .filter(id -> StringUtils.containsIgnoreCase(id.toString(), s))
                    .toList();
        }

        @Override public SelectableEntryList<ResourceLocation> getEntryList() {
            return new SelectableEntryList<>(EntityContainerEntry::new);
        }
    }
}