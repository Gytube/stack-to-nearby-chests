package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.ForEachContainerTask;
import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

// Remplace ScreenHandler → AbstractContainerMenu
@Mixin(AbstractContainerMenu.class)
@OnlyIn(Dist.CLIENT)
public abstract class ScreenHandlerMixin {

    // Remplace updateSlotStacks → initializeContents (Mojang mappings)
    @Inject(method = "initializeContents(ILjava/util/List;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onUpdateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo ci) {
        if (ForEachContainerTask.isRunning()) {
            ForEachContainerTask.getCurrentTask().onInventory((AbstractContainerMenu) (Object) this);
        }
    }

    // Remplace insertItem → moveItemStackTo, markDirty → setChanged (Mojang mappings)
    @Inject(method = "moveItemStackTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;setChanged()V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onInsertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast,
                              CallbackInfoReturnable<Boolean> cir, boolean bl, int i, Slot slot) {
        LockedSlots.onInsertItem(slot);
    }
}