package io.github.xiaocihua.stacktonearbychests.event;

import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge EVENT_BUS when a key is pressed or released.
 * Replaces the Fabric OnKeyCallback PRESS / RELEASE events.
 *
 * Listen with:
 *   @SubscribeEvent
 *   public void onKeyPress(OnKeyEvent.Press event) { ... }
 */
@OnlyIn(Dist.CLIENT)
public abstract class OnKeyEvent extends Event {

    private final int key;
    private InteractionResult result = InteractionResult.PASS;

    protected OnKeyEvent(int key) {
        this.key = key;
    }

    public int getKey()                                  { return key; }
    public InteractionResult getResult()                 { return result; }
    public void setResult(InteractionResult result)      { this.result = result; }

    @OnlyIn(Dist.CLIENT)
    public static class Press extends OnKeyEvent {
        public Press(int key) { super(key); }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Release extends OnKeyEvent {
        public Release(int key) { super(key); }
    }
}