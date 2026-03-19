package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Liste scrollable avec sélection multiple.
 * Remplace SelectableEntryList (LibGui WListPanel + WClippedPanel).
 */
public class SelectableEntryList<D> extends AbstractWidget {

    private static final int CELL_HEIGHT    = 20;
    private static final int SCROLLBAR_W    = 8;
    private static final int SCROLL_SPEED   = 3;

    private final Function<D, Entry<D>> supplier;
    private List<D> data = new ArrayList<>();
    private final List<D> selectedData = new ArrayList<>();
    private final Map<D, Entry<D>> configured = new HashMap<>();

    private int scrollOffset = 0;
    private Optional<Consumer<List<D>>> changedListener = Optional.empty();

    // ── Constructeurs ────────────────────────────────────────────────────────────

    public SelectableEntryList(Function<D, Entry<D>> supplier) {
        this(Collections.emptyList(), supplier);
    }

    public SelectableEntryList(Collection<D> data, Function<D, Entry<D>> supplier) {
        super(0, 0, 0, 0, net.minecraft.network.chat.Component.empty());
        this.supplier = supplier;
        this.data = new ArrayList<>(data);
    }

    // ── Données ──────────────────────────────────────────────────────────────────

    public void addData(Collection<D> newData) {
        this.data.addAll(newData);
        this.data = this.data.stream().distinct().collect(Collectors.toList());
        onChanged();
    }

    public void setData(List<D> data) {
        this.data = new ArrayList<>(data);
        scrollOffset = 0;
        configured.clear();
        onChanged();
    }

    public List<D> getSelectedData() { return selectedData; }

    public void removeSelected() {
        data.removeAll(selectedData);
        configured.keySet().removeAll(selectedData);
        selectedData.clear();
        onChanged();
    }

    public SelectableEntryList<D> setChangedListener(Consumer<List<D>> listener) {
        this.changedListener = Optional.ofNullable(listener);
        return this;
    }

    private void onChanged() {
        changedListener.ifPresent(l -> l.accept(data));
    }

    public void setSize(int w, int h) {
        this.width  = w;
        this.height = h;
    }

    // ── Rendu ────────────────────────────────────────────────────────────────────

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(); int y = getY();

        // Fond
        graphics.fill(x, y, x + width, y + height, 0xFF_262626);

        // Scissor (clipping)
        graphics.enableScissor(x, y, x + width, y + height);

        int cellW    = width - SCROLLBAR_W;
        int visible  = visibleCells();
        int startIdx = Math.max(0, Math.min(scrollOffset, Math.max(0, data.size() - visible)));

        for (int i = 0; i < visible; i++) {
            int idx = startIdx + i;
            if (idx >= data.size()) break;
            D d = data.get(idx);
            Entry<D> entry = configured.computeIfAbsent(d, supplier);
            entry.setParentList(this);
            entry.width  = cellW;
            entry.height = CELL_HEIGHT;
            entry.setPosition(x, y + i * CELL_HEIGHT);
            entry.render(graphics, mouseX, mouseY, partialTick);
        }

        graphics.disableScissor();

        // Scrollbar
        drawScrollbar(graphics, x + cellW, y);
    }

    private void drawScrollbar(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + SCROLLBAR_W, y + height, 0xFF_262626);
        if (data.size() <= visibleCells()) return;

        float ratio      = (float) visibleCells() / data.size();
        int handleH      = Math.max(6, (int) (height * ratio));
        float scrollRatio = (float) scrollOffset / Math.max(1, data.size() - visibleCells());
        int handleY      = y + (int) ((height - handleH) * scrollRatio);

        g.fill(x + 1, handleY, x + SCROLLBAR_W - 1, handleY + handleH, 0xFF_515151);
    }

    private int visibleCells() {
        return Math.max(1, height / CELL_HEIGHT);
    }

    // ── Interactions ─────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int x = getX(); int y = getY();
        int cellW   = width - SCROLLBAR_W;
        int visible = visibleCells();
        int startIdx = Math.max(0, Math.min(scrollOffset, Math.max(0, data.size() - visible)));

        for (int i = 0; i < visible; i++) {
            int idx = startIdx + i;
            if (idx >= data.size()) break;
            int entryY = y + i * CELL_HEIGHT;
            if (mouseX >= x && mouseX < x + cellW && mouseY >= entryY && mouseY < entryY + CELL_HEIGHT) {
                D d = data.get(idx);
                Entry<D> entry = configured.computeIfAbsent(d, supplier);
                entry.onClick();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) scrollY * SCROLL_SPEED,
                Math.max(0, data.size() - visibleCells())));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    // ── Entry ────────────────────────────────────────────────────────────────────

    public static abstract class Entry<D> {

        protected static final int TEXT_COLOR = 0xFF_F5F5F5;

        protected int width;
        protected int height;
        private int x;
        private int y;

        protected D data;
        protected boolean isSelected = false;
        private Optional<SelectableEntryList<D>> parentList = Optional.empty();

        public Entry(D data) {
            this.data = data;
        }

        public D getData() { return data; }

        public void setPosition(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }

        public void setParentList(SelectableEntryList<D> list) {
            this.parentList = Optional.of(list);
        }

        public void onClick() {
            isSelected = !isSelected;
            if (isSelected) parentList.ifPresent(p -> p.selectedData.add(data));
            else            parentList.ifPresent(p -> p.selectedData.remove(data));
        }

        /** Appelé par la liste — dessine le fond puis délègue à render(). */
        public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Fond sélection
            int bg = isSelected ? 0xFF_3A3A5C : 0xFF_1E1E1E;
            graphics.fill(x, y, x + width, y + height, bg);
            // Contenu spécifique à la sous-classe
            render(graphics, x, y, mouseX, mouseY);
        }

        /** Dessiner le contenu de l'entrée (icône, texte…). */
        public abstract void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY);
    }
}