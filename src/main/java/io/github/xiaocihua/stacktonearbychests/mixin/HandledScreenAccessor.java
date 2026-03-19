package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

// Remplace HandledScreen → AbstractContainerScreen (Mojang mappings)
@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {

    // Remplace "x" → "leftPos" (Mojang mappings)
    @Accessor("leftPos")
    int getX();

    @Accessor("leftPos")
    void setX(int x);

    // Remplace "y" → "topPos" (Mojang mappings)
    @Accessor("topPos")
    int getY();

    @Accessor("topPos")
    void setY(int y);

    // Remplace "focusedSlot" → "hoveredSlot" (Mojang mappings)
    @Accessor("hoveredSlot")
    @Nullable
    Slot getFocusedSlot();

    // Remplace "backgroundWidth" → "imageWidth" (Mojang mappings)
    @Accessor("imageWidth")
    int getBackgroundWidth();

    // Remplace "backgroundHeight" → "imageHeight" (Mojang mappings)
    @Accessor("imageHeight")
    int getBackgroundHeight();

    // Remplace getSlotAt → findSlot (Mojang mappings)
    @Invoker("findSlot")
    @Nullable
    Slot invokeGetSlotAt(double x, double y);
}