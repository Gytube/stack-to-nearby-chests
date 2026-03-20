package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.KeySequence;
import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import io.github.xiaocihua.stacktonearbychests.event.DisconnectEvent;
import io.github.xiaocihua.stacktonearbychests.event.SetScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Remplace MinecraftClient → Minecraft
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    // Remplace handleInputEvents → handleKeybinds, PlayerActionC2SPacket → ServerboundPlayerActionPacket (Mojang)
    @Inject(method = "handleKeybinds",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;<init>(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V"),
            cancellable = true)
    private void onSwapItemWithOffhand(CallbackInfo ci) {
        if (LockedSlots.onSwapItemWithOffhand() == InteractionResult.FAIL) {
            ci.cancel();
        }
    }

    // Remplace setScreen → setScreen (même nom, même signature)
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        SetScreenEvent event = new SetScreenEvent(screen);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        KeySequence.reCheckPressedKeys();
    }

    // Remplace onDisconnected → dropAllTasks (Mojang mappings) — à vérifier selon la version
    @Inject(method = "clearClientLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At("RETURN"))
    private void afterDisconnected(Screen screen, CallbackInfo ci) {
        DisconnectEvent event = new DisconnectEvent();
        NeoForge.EVENT_BUS.post(event);
    }
}