package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Bouton avec position mise à jour dynamiquement à chaque frame.
 * Remplace PosUpdatableButtonWidget (Fabric Screens API + LibGui Vec2i).
 */
public class PosUpdatableButtonWidget extends ImageButton {

    private final AbstractContainerScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater;

    private PosUpdatableButtonWidget(int width,
                                     int height,
                                     WidgetSprites sprites,
                                     OnPress pressAction,
                                     Component text,
                                     AbstractContainerScreen<?> parent,
                                     Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater) {
        super(0, 0, width, height, sprites, pressAction, text);
        this.parent     = parent;
        this.posUpdater = posUpdater;

        // Remplace Screens.getButtons(parent).add(this) — NeoForge expose addRenderableWidget
        // L'ajout est effectué dans Builder.build() via addRenderableWidget
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Mise à jour de la position avant le rendu
        posUpdater.ifPresent(updater -> {
            Vec2i pos = updater.apply((HandledScreenAccessor) parent);
            setPosition(pos.x(), pos.y());
        });
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    // ── Builder ──────────────────────────────────────────────────────────────────

    public static class Builder {
        private int width  = 16;
        private int height = 16;
        private WidgetSprites sprites;
        private OnPress pressAction = button -> {};
        @Nullable private Tooltip tooltip;
        private Component text = Component.empty();
        private final AbstractContainerScreen<?> parent;
        private Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        public Builder(AbstractContainerScreen<?> parent) {
            this.parent = parent;
        }

        public Builder setSize(int width, int height) {
            this.width = width; this.height = height; return this;
        }

        /** Remplace ButtonTextures → WidgetSprites (renommage NeoForge 1.21). */
        public Builder setTextures(WidgetSprites sprites) {
            this.sprites = sprites; return this;
        }

        public Builder setPressAction(OnPress pressAction) {
            this.pressAction = pressAction; return this;
        }

        public Builder setTooltip(@Nullable Component content) {
            if (content != null) this.tooltip = Tooltip.create(content);
            return this;
        }

        public Builder setText(Component text) {
            this.text = text; return this;
        }

        public Builder setPosUpdater(Function<HandledScreenAccessor, Vec2i> updater) {
            this.posUpdater = Optional.ofNullable(updater); return this;
        }

        public @Nullable PosUpdatableButtonWidget build() {
            if (sprites == null) return null;
            PosUpdatableButtonWidget button = new PosUpdatableButtonWidget(
                    width, height, sprites, pressAction, text, parent, posUpdater);
            button.setTooltip(tooltip);
            // Ajout au screen via la méthode NeoForge (à appeler depuis le screen lui-même)
            // Le screen doit appeler parent.addRenderableWidget(button)
            return button;
        }
    }
}