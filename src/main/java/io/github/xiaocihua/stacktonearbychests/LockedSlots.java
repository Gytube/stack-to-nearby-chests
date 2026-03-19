package io.github.xiaocihua.stacktonearbychests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.xiaocihua.stacktonearbychests.event.ClickSlotEvent;
import io.github.xiaocihua.stacktonearbychests.event.DisconnectEvent;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.MinecraftServerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.LOGGER;
import static java.util.function.Predicate.not;

@OnlyIn(Dist.CLIENT)
public class LockedSlots {
    private static final Path LOCKED_SLOTS_FOLDER = ModOptions.MOD_OPTIONS_DIR.resolve("locked-slots");

    public static final List<ResourceLocation> FAVORITE_ITEM_TAGS = List.of(
            ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "gold_badge"),
            ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "red_background"),
            ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "gold_border"),
            ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "iron_border"));

    private static HashSet<Integer> currentLockedSlots = new HashSet<>();
    private static boolean movingFavoriteItemStack = false;
    private static Slot quickMoveDestination;
    @Nullable
    private static ClickType actionBeingExecuted;

    private static Optional<Path> currentLockedSlotsFilePath = Optional.empty();

    public static void init() {
        // Remplace ClientPlayConnectionEvents.JOIN
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            Minecraft client = Minecraft.getInstance();
            currentLockedSlotsFilePath = getLockedSlotsFilePath(client);

            if (isEnabled()) {
                currentLockedSlotsFilePath.ifPresentOrElse(LockedSlots::read,
                        () -> LOGGER.info("Locked slots file path is empty"));
            }
        });

        // Remplace DisconnectCallback.EVENT
        NeoForge.EVENT_BUS.addListener((DisconnectEvent event) -> {
            if (isEnabled()) {
                currentLockedSlotsFilePath.ifPresentOrElse(LockedSlots::write,
                        () -> LOGGER.info("Locked slots file path is empty"));
            }
            currentLockedSlots.clear();
            currentLockedSlotsFilePath = Optional.empty();
        });

        // Remplace registerOnScreen pour markAsFavoriteKey
        ModOptions.get().keymap.markAsFavoriteKey.registerOnScreen(
                AbstractContainerScreen.class,
                screen -> {
                    Minecraft client = Minecraft.getInstance();
                    double x = client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
                    double y = client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();
                    Slot slot = ((HandledScreenAccessor) screen).invokeGetSlotAt(x, y);
                    if (isLockable(slot) && slot.hasItem()) {
                        if (ModOptions.get().appearance.enableFavoritingSoundEffect.booleanValue()) {
                            client.getSoundManager().play(
                                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        }
                        if (!unLock(slot)) {
                            lock(slot);
                        }
                    }
                },
                net.minecraft.world.InteractionResult.FAIL);

        // Remplace ScreenEvents.AFTER_INIT
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof AbstractContainerScreen<?> handledScreen) {
                if (!isEnabled()) {
                    currentLockedSlots.clear();
                }
                refresh(handledScreen.getMenu());

                // Remplace ScreenEvents.remove
                NeoForge.EVENT_BUS.addListener((ScreenEvent.Closing closeEvent) -> {
                    if (closeEvent.getScreen() == handledScreen) {
                        movingFavoriteItemStack = false;
                    }
                });
            }
        });

        // Remplace ClickSlotCallback.BEFORE
        NeoForge.EVENT_BUS.addListener((ClickSlotEvent.Before event) -> {
            @Nullable Slot slot = event.getSlotId() < 0
                    ? null
                    : event.getPlayer().containerMenu.getSlot(event.getSlotId());

            if (isLocked(slot) && (
                    event.getClickType() == ClickType.PICKUP
                            && ModOptions.get().behavior.favoriteItemsCannotBePickedUp.booleanValue()
                    || event.getClickType() == ClickType.QUICK_MOVE
                            && ModOptions.get().behavior.favoriteItemStacksCannotBeQuickMoved.booleanValue()
                    || event.getClickType() == ClickType.SWAP
                            && ModOptions.get().behavior.favoriteItemStacksCannotBeSwapped.booleanValue()
                    || event.getClickType() == ClickType.THROW
                            && ModOptions.get().behavior.favoriteItemStacksCannotBeThrown.booleanValue()
            )) {
                event.setResult(net.minecraft.world.InteractionResult.FAIL);
                event.setCanceled(true);
                return;
            }

            actionBeingExecuted = event.getClickType();
        });

        // Remplace ClickSlotCallback.AFTER
        NeoForge.EVENT_BUS.addListener((ClickSlotEvent.After event) -> {
            afterClickSlot(event.getSlotId(), event.getButton(), event.getClickType(), event.getPlayer());
        });
    }

    private static void read(Path path) {
        LOGGER.info("Reading locked slot indices from {}", path.getFileName());
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<HashSet<Integer>>() {}.getType();
            currentLockedSlots = new Gson().fromJson(reader, type);
        } catch (NoSuchFileException e) {
            LOGGER.info("Locked slots file does not exist");
        } catch (IOException e) {
            LOGGER.error("Failed to read locked slots file", e);
        }
    }

    private static void write(Path path) {
        LOGGER.info("Writing locked slot indices to {}", path.getFileName());
        try {
            Files.createDirectories(LOCKED_SLOTS_FOLDER);
            String json = new Gson().toJson(currentLockedSlots);
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to write locked slots file", e);
        }
    }

    private static Optional<Path> getLockedSlotsFilePath(Minecraft client) {
        // Remplace client.getServer() → client.getSingleplayerServer()
        IntegratedServer integratedServer = client.getSingleplayerServer();
        // Remplace client.getCurrentServerEntry() → client.getCurrentServer()
        ServerData currentServerEntry = client.getCurrentServer();
        String fileName;
        if (integratedServer != null) {
            fileName = ((MinecraftServerAccessor) integratedServer).getSession()
                    .getLevelId().concat(".json");
        } else if (currentServerEntry != null) {
            fileName = currentServerEntry.ip.concat(".json").replace(":", "colon");
        } else {
            LOGGER.info("Could not get level name or server address");
            return Optional.empty();
        }
        return Optional.of(LOCKED_SLOTS_FOLDER.resolve(fileName));
    }

    private static boolean isEnabled() {
        return ModOptions.get().behavior.enableItemFavoriting.booleanValue();
    }

    private static boolean lock(@Nullable Slot slot) {
        return isLockable(slot) && lock(slot.getContainerSlot());
    }

    private static boolean lock(int slotIndex) {
        return isLockable(slotIndex) && currentLockedSlots.add(slotIndex);
    }

    private static boolean unLock(@Nullable Slot slot) {
        return isLockable(slot) && unLock(slot.getContainerSlot());
    }

    private static boolean unLock(int slotIndex) {
        return isLockable(slotIndex) && currentLockedSlots.remove(slotIndex);
    }

    private static boolean setLocked(int slotIndex, boolean locked) {
        return locked ? lock(slotIndex) : unLock(slotIndex);
    }

    public static boolean isLocked(@Nullable Slot slot) {
        return isLockable(slot) && isLocked(slot.getContainerSlot());
    }

    public static boolean isLocked(int slotIndex) {
        return isLockable(slotIndex) && currentLockedSlots.contains(slotIndex);
    }

    private static boolean isLockable(@Nullable Slot slot) {
        return isEnabled()
                && slot != null
                && slot.container instanceof Inventory
                && !Minecraft.getInstance().player.getAbilities().instabuild;
    }

    private static boolean isLockable(int slotIndex) {
        return isEnabled()
                && slotIndex >= 0
                && slotIndex != 39 // Head
                && slotIndex != 38 // Chest
                && slotIndex != 37 // Legs
                && slotIndex != 36; // Feet
    }

    public static void onSetStack(int slotIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            if (actionBeingExecuted == null) {
                if (!StackToNearbyChests.IS_EASY_SHULKER_BOXES_MOD_LOADED) {
                    unLock(slotIndex);
                }
            } else if (actionBeingExecuted == ClickType.THROW) {
                unLock(slotIndex);
            } else if (actionBeingExecuted == ClickType.PICKUP_ALL) {
                if (unLock(slotIndex)) {
                    movingFavoriteItemStack = true;
                }
            }
        }
    }

    public static void onInsertItem(Slot destination) {
        quickMoveDestination = destination;
    }

    private static void afterClickSlot(int slotId, int button, ClickType clickType, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        @Nullable Slot slot = slotId < 0 ? null : menu.getSlot(slotId);

        switch (clickType) {
            case PICKUP -> {
                if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
                    movingFavoriteItemStack = false;
                }
                if (slot == null) break;

                ItemStack cursorStack = menu.getCarried();
                ItemStack slotStack = slot.getItem();
                if (movingFavoriteItemStack) {
                    if (cursorStack.isEmpty()) {
                        lock(slot);
                        movingFavoriteItemStack = false;
                    } else if (!ItemStack.isSameItemSameComponents(cursorStack, slotStack)) {
                        if (!isLocked(slot)) movingFavoriteItemStack = false;
                        lock(slot);
                    }
                } else {
                    if (isLocked(slot) && slotStack.isEmpty()) {
                        unLock(slot);
                        movingFavoriteItemStack = true;
                    } else if (!cursorStack.isEmpty()
                            && !ItemStack.isSameItemSameComponents(cursorStack, slotStack)) {
                        if (isLocked(slot)) movingFavoriteItemStack = true;
                        unLock(slot);
                    }
                }
            }
            case QUICK_MOVE -> {
                if (slot == null) break;
                if (isLocked(slot) && !slot.hasItem()) {
                    unLock(slot);
                    lock(quickMoveDestination);
                }
            }
            case SWAP -> {
                boolean isFromSlotLocked = unLock(slot);
                boolean isToSlotLocked = unLock(button);
                if (isFromSlotLocked) lock(button);
                if (isToSlotLocked) lock(slot);
            }
            case QUICK_CRAFT -> {
                if (menu.getCarried().isEmpty()) {
                    movingFavoriteItemStack = false;
                } else if (movingFavoriteItemStack) {
                    lock(slot);
                }
            }
        }

        actionBeingExecuted = null;
    }

    private static boolean refresh(AbstractContainerMenu menu) {
        return menu.slots.stream()
                .filter(not(Slot::hasItem))
                .map(LockedSlots::unLock)
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    public static void onSetGameMode(GameType gameMode) {
        if (gameMode == GameType.CREATIVE) {
            currentLockedSlots.clear();
        }
    }

    public static void drawFavoriteItemStyle(GuiGraphics context, Slot slot) {
        ModOptions options = ModOptions.get();

        if (!(options.appearance.alwaysShowMarkersForFavoritedItems.booleanValue()
                || options.keymap.showMarkersForFavoritedItemsKey.isPressed()
                || options.keymap.markAsFavoriteKey.isPressed())) {
            return;
        }

        ResourceLocation id = options.appearance.favoriteItemStyle;
        ResourceLocation sprite = ResourceLocation.fromNamespaceAndPath(
                id.getNamespace(), "textures/item/" + id.getPath() + ".png");

        if (isLocked(slot)) {
            // Remplace context.drawTexture(RenderPipelines.GUI_TEXTURED, ...)
            context.blit(sprite, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
        }
    }

    public static net.minecraft.world.InteractionResult beforeDropSelectedItem(int selectedSlotIndex) {
        if (isLocked(selectedSlotIndex)
                && ModOptions.get().behavior.favoriteItemStacksCannotBeThrown.booleanValue()) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public static void afterDropSelectedItem(int selectedSlotIndex) {
        if (isLocked(selectedSlotIndex)
                && !Minecraft.getInstance().player.inventoryMenu.slots
                        .get(selectedSlotIndex).hasItem()) {
            unLock(selectedSlotIndex);
        }
    }

    public static net.minecraft.world.InteractionResult onSwapItemWithOffhand() {
        int selectedSlotIndex = Minecraft.getInstance().player.getInventory().selected;
        boolean isSelectedSlotLocked = isLocked(selectedSlotIndex);

        if (isSelectedSlotLocked
                && ModOptions.get().behavior.favoriteItemsCannotBeSwappedWithOffhand.booleanValue()) {
            return net.minecraft.world.InteractionResult.FAIL;
        }

        // Remplace PlayerInventory.OFF_HAND_SLOT → Inventory.SLOT_OFFHAND
        setLocked(selectedSlotIndex, isLocked(Inventory.SLOT_OFFHAND));
        setLocked(Inventory.SLOT_OFFHAND, isSelectedSlotLocked);

        return net.minecraft.world.InteractionResult.PASS;
    }
}