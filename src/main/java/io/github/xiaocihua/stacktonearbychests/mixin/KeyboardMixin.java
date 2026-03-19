package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Remplace Keyboard → KeyboardHandler (Mojang mappings)
@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {

    // Remplace onKey → keyPress, debugCrashStartTime → debugCrashKeyTime (Mojang mappings)
    @Inject(method = "keyPress",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J",
                    ordinal = 0),
            cancellable = true)
    private void onOnKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        InteractionResult result = switch (action) {
            case 0 -> {
                OnKeyEvent.Release event = new OnKeyEvent.Release(key);
                NeoForge.EVENT_BUS.post(event);
                yield event.getResult();
            }
            case 1 -> {
                OnKeyEvent.Press event = new OnKeyEvent.Press(key);
                NeoForge.EVENT_BUS.post(event);
                yield event.getResult();
            }
            default -> InteractionResult.PASS;
        };

        if (result == InteractionResult.FAIL) {
            ci.cancel();
        }
    }
}