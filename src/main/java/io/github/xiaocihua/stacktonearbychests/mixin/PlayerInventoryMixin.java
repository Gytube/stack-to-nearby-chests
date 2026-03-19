package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Remplace PlayerInventory → Inventory (Mojang mappings)
@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow @Final public Player player;

    // Remplace setStack → setItem (Mojang mappings)
    @Inject(method = "setItem", at = @At("HEAD"))
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        // Remplace getEntityWorld().isClient() → level.isClientSide() (Mojang mappings)
        if (player.level().isClientSide()) {
            LockedSlots.onSetStack(slot, stack);
        }
    }
}