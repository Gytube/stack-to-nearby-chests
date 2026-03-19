package io.github.xiaocihua.stacktonearbychests.mixin;

import io.github.xiaocihua.stacktonearbychests.KeySequence;
import io.github.xiaocihua.stacktonearbychests.event.OnKeyEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Remplace Mouse → MouseHandler (Mojang mappings)
@Mixin(MouseHandler.class)
public abstract class MouseMixin {

    // Remplace onMouseButton → onPress, MinecraftClient.getWindow() → Minecraft.getInstance().getWindow() (Mojang)
    @Inject(method = "onPress",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;",
                    ordinal = 0),
            cancellable = true)
    private void onOnMouseButton(long window, int button, int action, int modifiers, CallbackInfo ci) {
        InteractionResult result = switch (action) {
            case 0 -> {
                OnKeyEvent.Release event = new OnKeyEvent.Release(button - KeySequence.MOUSE_BUTTON_CODE_OFFSET);
                NeoForge.EVENT_BUS.post(event);
                yield event.getResult();
            }
            case 1 -> {
                OnKeyEvent.Press event = new OnKeyEvent.Press(button - KeySequence.MOUSE_BUTTON_CODE_OFFSET);
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