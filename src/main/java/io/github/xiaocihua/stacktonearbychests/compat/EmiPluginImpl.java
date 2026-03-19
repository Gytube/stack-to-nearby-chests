package io.github.xiaocihua.stacktonearbychests.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.github.xiaocihua.stacktonearbychests.ModOptions;
import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests;
import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class EmiPluginImpl implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            ModOptions.Appearance appearanceOption = ModOptions.get().appearance;

            if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
                int parentX = ((HandledScreenAccessor) screen).getX();
                int parentY = ((HandledScreenAccessor) screen).getY();

                if (appearanceOption.showStackToNearbyContainersButton.booleanValue()) {
                    consumer.accept(new Bounds(
                            parentX + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                            parentY + appearanceOption.stackToNearbyContainersButtonPosY.intValue(),
                            16, 16
                    ));
                }

                if (appearanceOption.showRestockFromNearbyContainersButton.booleanValue()) {
                    consumer.accept(new Bounds(
                            parentX + appearanceOption.restockFromNearbyContainersButtonPosX.intValue(),
                            parentY + appearanceOption.restockFromNearbyContainersButtonPosY.intValue(),
                            16, 16
                    ));
                }
            } else if (StackToNearbyChests.isContainerScreen(screen)) {
                if (appearanceOption.showQuickStackButton.booleanValue()) {
                    Vec2i buttonPos = StackToNearbyChests.getAbsolutePos(
                            (HandledScreenAccessor) screen,
                            appearanceOption.quickStackButtonPosX,
                            appearanceOption.quickStackButtonPosY);
                    consumer.accept(new Bounds(buttonPos.x(), buttonPos.y(), 16, 16));
                }

                if (appearanceOption.showRestockButton.booleanValue()) {
                    Vec2i buttonPos = StackToNearbyChests.getAbsolutePos(
                            (HandledScreenAccessor) screen,
                            appearanceOption.restockButtonPosX,
                            appearanceOption.restockButtonPosY);
                    consumer.accept(new Bounds(buttonPos.x(), buttonPos.y(), 16, 16));
                }
            }
        });
    }
}