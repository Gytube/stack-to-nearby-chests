package io.github.xiaocihua.stacktonearbychests.event;

import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge EVENT_BUS when the client disconnects.
 * Replaces the Fabric DisconnectCallback.
 *
 * Listen with:
 *   @SubscribeEvent
 *   public void onDisconnect(DisconnectEvent event) { ... }
 */
public class DisconnectEvent extends Event {
    // Pas de données supplémentaires nécessaires
}