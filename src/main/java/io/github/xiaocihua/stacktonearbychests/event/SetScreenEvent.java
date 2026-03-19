package io.github.xiaocihua.stacktonearbychests.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge EVENT_BUS when the client screen is about to change.
 * Replaces the Fabric SetScreenCallback.
 *
 * Listen with:
 * 
 * @SubscribeEvent
 *                 public void onSetScreen(SetScreenEvent event) { ... }
 */
@OnlyIn(Dist.CLIENT)
public class SetScreenEvent extends Event {

    private final Screen screen;
    private InteractionResult result = InteractionResult.PASS;

    public SetScreenEvent(Screen screen) {
        this.screen = screen;
    }

    private boolean denied;

    public void deny() {
        this.denied = true;
    }

    public boolean isDenied() {
        return denied;
    }

    public Screen getScreen() {
        return screen;
    }

    public InteractionResult getResult() {
        return result;
    }

    public void setResult(InteractionResult result) {
        this.result = result;
    }
}