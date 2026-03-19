package io.github.xiaocihua.stacktonearbychests.event;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on the NeoForge MOD_BUS before and after a slot is clicked.
 * Replaces the Fabric ClickSlotCallback BEFORE / AFTER events.
 *
 * Listen with:
 *   @SubscribeEvent
 *   public void onClickSlot(ClickSlotEvent.Before event) { ... }
 */
public abstract class ClickSlotEvent extends Event {

    private final int containerId;
    private final int slotId;
    private final int button;
    private final ClickType clickType;
    private final Player player;
    private InteractionResult result = InteractionResult.PASS;

    protected ClickSlotEvent(int containerId, int slotId, int button, ClickType clickType, Player player) {
        this.containerId = containerId;
        this.slotId = slotId;
        this.button = button;
        this.clickType = clickType;
        this.player = player;
    }

    public int getContainerId()  { return containerId; }
    public int getSlotId()       { return slotId; }
    public int getButton()       { return button; }
    public ClickType getClickType() { return clickType; }
    public Player getPlayer()    { return player; }

    public InteractionResult getResult()                      { return result; }
    public void setResult(InteractionResult result)           { this.result = result; }

    /** Fired before the slot click is processed. Cancellable. */
    public static class Before extends ClickSlotEvent implements ICancellableEvent {
        public Before(int containerId, int slotId, int button, ClickType clickType, Player player) {
            super(containerId, slotId, button, clickType, player);
        }
    }

    /** Fired after the slot click has been processed. */
    public static class After extends ClickSlotEvent {
        public After(int containerId, int slotId, int button, ClickType clickType, Player player) {
            super(containerId, slotId, button, clickType, player);
        }
    }
}