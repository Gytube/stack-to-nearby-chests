/**package io.github.xiaocihua.stacktonearbychests.compat;

import io.github.xiaocihua.stacktonearbychests.ModOptions;
import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests;
import io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.Vec2i;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.ArrayList;
import java.util.List;

public class ReiClientPluginImpl implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones exclusionZones) {
        exclusionZones.register(Screen.class, screen -> {
            List<Rectangle> zones = new ArrayList<>();
            ModOptions.Appearance appearanceOption = ModOptions.get().appearance;

            if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
                int parentX = ((HandledScreenAccessor) screen).getX();
                int parentY = ((HandledScreenAccessor) screen).getY();

                if (appearanceOption.showStackToNearbyContainersButton.booleanValue()) {
                    zones.add(new Rectangle(
                            parentX + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                            parentY + appearanceOption.stackToNearbyContainersButtonPosY.intValue(),
                            16, 16
                    ));
                }

                if (appearanceOption.showRestockFromNearbyContainersButton.booleanValue()) {
                    zones.add(new Rectangle(
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
                    zones.add(new Rectangle(buttonPos.x(), buttonPos.y(), 16, 16));
                }

                if (appearanceOption.showRestockButton.booleanValue()) {
                    Vec2i buttonPos = StackToNearbyChests.getAbsolutePos(
                            (HandledScreenAccessor) screen,
                            appearanceOption.restockButtonPosX,
                            appearanceOption.restockButtonPosY);
                    zones.add(new Rectangle(buttonPos.x(), buttonPos.y(), 16, 16));
                }
            }
            return zones;
        });
    }
}**/