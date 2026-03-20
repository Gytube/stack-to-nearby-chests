package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

public class PosUpdatableButtonWidget extends ImageButton {

    private final AbstractContainerScreen<?> parent;
    private final Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater;

    private PosUpdatableButtonWidget(int width, int height,
                                     WidgetSprites sprites,
                                     OnPress pressAction,
                                     Component text,
                                     AbstractContainerScreen<?> parent,
                                     Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater) {
        super(0, 0, width, height, sprites, pressAction, text);
        this.parent     = parent;
        this.posUpdater = posUpdater;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        posUpdater.ifPresent(u -> {
            Vec2i pos = u.apply((HandledScreenAccessor) parent);
            setPosition(pos.x(), pos.y());
        });
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Ajoute le bouton au screen via réflexion — contourne l'accès protégé de
     * addRenderableWidget() qui n'est pas public dans AbstractContainerScreen.
     */
    private static void addToScreen(AbstractContainerScreen<?> screen, PosUpdatableButtonWidget button) {
        try {
            Method m = net.minecraft.client.gui.screens.Screen.class
                    .getDeclaredMethod("addRenderableWidget",
                            net.minecraft.client.gui.components.events.GuiEventListener.class);
            m.setAccessible(true);
            m.invoke(screen, button);
        } catch (Exception e) {
            // Fallback : ajout manuel à la liste de renderables via reflection sur children
            try {
                var renderables = screen.getClass().getSuperclass().getDeclaredField("renderables");
                renderables.setAccessible(true);
                @SuppressWarnings("unchecked")
                var list = (java.util.List<Object>) renderables.get(screen);
                list.add(button);

                var children = net.minecraft.client.gui.screens.Screen.class
                        .getDeclaredField("children");
                children.setAccessible(true);
                @SuppressWarnings("unchecked")
                var clist = (java.util.List<Object>) children.get(screen);
                clist.add(button);
            } catch (Exception ex) {
                io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.LOGGER
                        .error("Failed to add button to screen", ex);
            }
        }
    }

    public static class Builder {
        private int width = 16, height = 16;
        private WidgetSprites sprites;
        private OnPress pressAction = b -> {};
        @Nullable private Tooltip tooltip;
        private Component text = Component.empty();
        private final AbstractContainerScreen<?> parent;
        private Optional<Function<HandledScreenAccessor, Vec2i>> posUpdater = Optional.empty();

        public Builder(AbstractContainerScreen<?> parent) { this.parent = parent; }

        public Builder setSize(int w, int h)          { this.width = w; this.height = h; return this; }
        public Builder setTextures(WidgetSprites s)   { this.sprites = s; return this; }
        public Builder setPressAction(OnPress a)      { this.pressAction = a; return this; }
        public Builder setText(Component t)           { this.text = t; return this; }

        public Builder setTooltip(@Nullable Component content) {
            if (content != null) this.tooltip = Tooltip.create(content);
            return this;
        }

        public Builder setPosUpdater(Function<HandledScreenAccessor, Vec2i> u) {
            this.posUpdater = Optional.ofNullable(u); return this;
        }

        public @Nullable PosUpdatableButtonWidget build() {
            if (sprites == null) return null;
            var button = new PosUpdatableButtonWidget(
                    width, height, sprites, pressAction, text, parent, posUpdater);
            button.setTooltip(tooltip);
            addToScreen(parent, button);
            return button;
        }
    }
}