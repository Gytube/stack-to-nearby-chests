package io.github.xiaocihua.stacktonearbychests;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.LinkedList;
import java.util.Queue;

public class EndWorldTickExecutor {

    private static final Queue<Runnable> tasks = new LinkedList<>();

    public static void init() {
        // Remplace ClientTickEvents.END_WORLD_TICK
        NeoForge.EVENT_BUS.addListener(EndWorldTickExecutor::onWorldTickEnd);
    }

    private static void onWorldTickEnd(ClientTickEvent.Post event) {
        while (tasks.peek() != null) {
            tasks.poll().run();
        }
    }

    public static void execute(Runnable task) {
        tasks.add(task);
    }
}