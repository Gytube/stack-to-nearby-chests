package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import io.github.xiaocihua.stacktonearbychests.event.ClickSlotEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Remplace ClientPlayerInteractionManager → MultiPlayerGameMode
@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {

    // Remplace clickSlot → handleInventoryMouseClick, ScreenHandler.slots → AbstractContainerMenu.slots (Mojang)
    @Inject(method = "handleInventoryMouseClick",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;slots:Ljava/util/List;"),
            cancellable = true)
    private void beforeClickSlot(int containerId, int slotId, int button, ClickType clickType,
                                 Player player, CallbackInfo ci) {
        ClickSlotEvent.Before event = new ClickSlotEvent.Before(containerId, slotId, button, clickType, player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    // Remplace sendPacket → send (Mojang mappings)
    @Inject(method = "handleInventoryMouseClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"),
            cancellable = true)
    private void afterClickSlot(int containerId, int slotId, int button, ClickType clickType,
                                Player player, CallbackInfo ci) {
        ClickSlotEvent.After event = new ClickSlotEvent.After(containerId, slotId, button, clickType, player);
        NeoForge.EVENT_BUS.post(event);
    }

    // Remplace setGameMode → setLocalMode (Mojang mappings)
    @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;)V", at = @At("TAIL"))
    private void onSetGameMode(GameType gameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }

    // Remplace setGameModes → setGameModes (même nom, paramètres changent)
    @Inject(method = "setGameModes(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V",
            at = @At("TAIL"))
    private void onSetGameModes(GameType gameMode, GameType previousGameMode, CallbackInfo ci) {
        LockedSlots.onSetGameMode(gameMode);
    }
}