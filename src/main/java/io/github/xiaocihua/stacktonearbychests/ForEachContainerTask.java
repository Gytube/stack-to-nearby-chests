package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyEvent;
import io.github.xiaocihua.stacktonearbychests.event.SetScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public abstract class ForEachContainerTask {

    private static final ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(1);
    private static ForEachContainerTask currentTask;

    protected final Minecraft client;
    protected final LocalPlayer player;
    protected final Consumer<AbstractContainerMenu> action;

    private boolean interrupted;
    private final int searchInterval;

    @Nullable
    private ForEachContainerTask after;

    public ForEachContainerTask(Minecraft client, LocalPlayer player, Consumer<AbstractContainerMenu> action) {
        this.client = client;
        this.player = player;
        this.action = action;
        searchInterval = ModOptions.get().behavior.searchInterval.intValue();
    }

    public static void init() {
        // Remplace SetScreenCallback.EVENT
        NeoForge.EVENT_BUS.addListener((SetScreenEvent event) -> {
            if (isRunning()) {
                if (event.getScreen() instanceof DeathScreen) {
                    currentTask.interrupt();
                    event.setResult(InteractionResult.PASS);
                    return;
                }
                event.setResult(InteractionResult.FAIL);
                event.deny();
            }
        });

        // Remplace OnKeyCallback.PRESS
        NeoForge.EVENT_BUS.addListener((OnKeyEvent.Press event) -> {
            if (isRunning()) {
                if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
                    currentTask.interrupt();
                }
                event.setResult(InteractionResult.FAIL);
            }
        });

        // Remplace ClientReceiveMessageEvents.GAME
        NeoForge.EVENT_BUS.addListener((ClientChatReceivedEvent event) -> {
            if (isRunning()
                    && event.getMessage().getContents() instanceof TranslatableContents translatable
                    && translatable.getKey().equals("container.isLocked")) {
                getCurrentTask().openNextContainer();
            }
        });
    }

    public static ForEachContainerTask getCurrentTask() {
        return currentTask;
    }

    public static boolean isRunning() {
        return currentTask != null;
    }

    public void start() {
        currentTask = this;
        openNextContainerExceptionHandled();
    }

    protected void stop() {
        // Remplace player.closeHandledScreen()
        player.closeContainer();
        TIMER.getQueue().clear();
        currentTask = null;
    }

    public void interrupt() {
        sendChatMessage("stacktonearbychests.message.actionInterrupted");
        interrupted = true;
    }

    public void onInventory(AbstractContainerMenu menu) {
        clearTimeout();
        action.accept(menu);
        openNextContainer();
    }

    private void openNextContainer() {
        if (interrupted) {
            stop();
            return;
        }

        if (searchInterval == 0) {
            openNextContainerExceptionHandled();
        } else {
            TIMER.schedule(() -> client.execute(this::openNextContainerExceptionHandled),
                    searchInterval, TimeUnit.MILLISECONDS);
        }
    }

    private void openNextContainerExceptionHandled() {
        try {
            if (findAndOpenNextContainer()) {
                setTimeout();
            } else if (after != null) {
                after.start();
            } else {
                stop();
            }
        } catch (Exception e) {
            sendChatMessage("stacktonearbychests.message.exceptionOccurred");
            StackToNearbyChests.LOGGER.error("An exception occurred", e);
            stop();
        }
    }

    protected abstract boolean findAndOpenNextContainer();

    private void setTimeout() {
        TIMER.schedule(() -> client.execute(() -> {
            sendChatMessage("stacktonearbychests.message.interruptedByTimeout");
            stop();
        }), 2, TimeUnit.SECONDS);
    }

    private void clearTimeout() {
        TIMER.getQueue().clear();
    }

    public void thenStart(ForEachContainerTask after) {
        this.after = after;
    }

    private void sendChatMessage(String key) {
        // Remplace client.inGameHud.getChatHud().addMessage()
        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable(key));
    }
}