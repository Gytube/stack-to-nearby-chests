package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.ClickSlotEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.LOGGER;
import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.currentStackToNearbyContainersButton;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toSet;

public class InventoryActions {

    public static void init() {
        // Remplace ClickSlotCallback.BEFORE
        NeoForge.EVENT_BUS.addListener((ClickSlotEvent.Before event) -> {
            if (event.getSlotId() == -999
                    && event.getClickType() == ClickType.PICKUP
                    && currentStackToNearbyContainersButton
                            .map(AbstractWidget::isHovered).orElse(false)) {
                event.setResult(InteractionResult.FAIL);
                event.setCanceled(true);
            }
        });
    }

    public static void stackToNearbyContainers() {
        forEachContainer(InventoryActions::quickStack,
                ModOptions.get().behavior.stackingTargets,
                ModOptions.get().behavior.stackingTargetEntities);
    }

    public static void stackToNearbyContainers(Item item) {
        forEachContainer(menu -> quickStack(menu, item),
                ModOptions.get().behavior.stackingTargets,
                ModOptions.get().behavior.stackingTargetEntities);
    }

    public static void restockFromNearbyContainers() {
        forEachContainer(InventoryActions::restock,
                ModOptions.get().behavior.restockingSources,
                ModOptions.get().behavior.restockingSourceEntities);
    }

    public static boolean canMerge(ItemStack stack, ItemStack otherStack) {
        // Remplace ItemStack.areItemsAndComponentsEqual → isSameItemSameComponents
        return stack.getCount() < stack.getMaxStackSize()
                && ItemStack.isSameItemSameComponents(stack, otherStack);
    }

    public static void forEachContainer(Consumer<AbstractContainerMenu> action,
                                        Collection<String> blockFilter,
                                        Collection<String> entityFilter) {
        Minecraft client = Minecraft.getInstance();

        Entity cameraEntity = client.getCameraEntity();
        Level world = client.level;
        MultiPlayerGameMode interactionManager = client.gameMode;
        LocalPlayer player = client.player;

        if (cameraEntity == null || world == null || interactionManager == null || player == null) {
            LOGGER.info("cameraEntity: {}, world: {}, interactionManager: {}, player: {}",
                    cameraEntity, world, interactionManager, player);
            return;
        } else if (player.isSpectator()) {
            LOGGER.info("The player is in spectator mode");
            return;
        } else if (player.isCrouching()) {
            LOGGER.info("The player is sneaking");
            return;
        }

        var task = new ForEachBlockContainerTask(client, cameraEntity, world, player,
                interactionManager, action, blockFilter);

        if (ModOptions.get().behavior.supportForContainerEntities.booleanValue()
                && !player.isPassenger()) {
            task.thenStart(new ForEachEntityContainerTask(client, player, action,
                    cameraEntity, world, interactionManager, entityFilter));
        }

        task.start();
    }

    public static void quickStack(AbstractContainerMenu menu) {
        var slots = SlotsInMenu.of(menu);

        Set<Item> itemsInContainer = slots.containerSlots().stream()
                .map(slot -> slot.getItem().getItem())
                .filter(item -> !ModOptions.get().behavior.itemsThatWillNotBeStacked
                        .contains(BuiltInRegistries.ITEM.getKey(item).toString()))
                .collect(toSet());

        moveAll(menu, slots.playerSlots(), itemsInContainer);
    }

    public static void quickStack(AbstractContainerMenu menu, Item item) {
        var slots = SlotsInMenu.of(menu);

        boolean hasSameTypeItems = slots.containerSlots().stream()
                .anyMatch(slot -> slot.getItem().is(item));

        if (hasSameTypeItems) {
            moveAll(menu, slots.playerSlots(), Set.of(item));
        }
    }

    private static void moveAll(AbstractContainerMenu menu,
                                List<Slot> playerSlots,
                                Set<Item> itemsToBeMoved) {
        playerSlots.stream()
                .filter(slot -> !(ModOptions.get().behavior.doNotQuickStackItemsFromTheHotbar.booleanValue()
                        // Remplace PlayerInventory.isValidHotbarIndex → Inventory.isHotbarSlot
                        && Inventory.isHotbarSlot(slot.getContainerSlot())))
                .filter(not(InventoryActions::isSlotLocked))
                .filter(slot -> itemsToBeMoved.contains(slot.getItem().getItem()))
                .filter(slot -> slot.mayPickup(Minecraft.getInstance().player))
                .filter(Slot::hasItem)
                .forEach(slot -> quickMove(menu, slot));
    }

    public static void restock(AbstractContainerMenu menu) {
        var slots = SlotsInMenu.of(menu);
        slots.playerSlots().stream()
                .filter(Slot::hasItem)
                .filter(slot -> slot.getItem().isStackable())
                .filter(slot -> !ModOptions.get().behavior.itemsThatWillNotBeRestocked
                        .contains(BuiltInRegistries.ITEM
                                .getKey(slot.getItem().getItem())
                                .toString()))
                .forEach(slot -> slots.containerSlots().stream()
                        .filter(containerSlot -> ItemStack.isSameItemSameComponents(
                                slot.getItem(), containerSlot.getItem()))
                        .peek(containerSlot -> {
                            pickup(menu, containerSlot);
                            pickup(menu, slot);
                        })
                        .filter(containerSlot -> !menu.getCarried().isEmpty())
                        .findFirst()
                        .ifPresent(containerSlot -> pickup(menu, containerSlot))
                );
    }

    public static void quickMove(AbstractContainerMenu menu, Slot slot) {
        Minecraft client = Minecraft.getInstance();
        // Remplace interactionManager.clickSlot → gameMode.handleInventoryMouseClick
        client.gameMode.handleInventoryMouseClick(
                menu.containerId, slot.index,
                GLFW.GLFW_MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE,
                client.player);
    }

    public static void pickup(AbstractContainerMenu menu, Slot slot) {
        Minecraft client = Minecraft.getInstance();
        client.gameMode.handleInventoryMouseClick(
                menu.containerId, slot.index,
                GLFW.GLFW_MOUSE_BUTTON_LEFT, ClickType.PICKUP,
                client.player);
    }

    // ── Slots helper ─────────────────────────────────────────────────────────────

    private record SlotsInMenu(List<Slot> playerSlots, List<Slot> containerSlots) {

        static SlotsInMenu of(AbstractContainerMenu menu) {
            // Remplace slot.inventory instanceof PlayerInventory → slot.container instanceof Inventory
            Map<Boolean, List<Slot>> inventories = menu.slots.stream()
                    .collect(partitioningBy(slot -> slot.container instanceof Inventory));

            return new SlotsInMenu(inventories.get(true), inventories.get(false));
        }
    }

    // ── IPN + LockedSlots check ───────────────────────────────────────────────────

    private static boolean isSlotLocked(Slot slot) {
        if (StackToNearbyChests.IS_IPN_MOD_LOADED) {
            try {
                Class<?> clazz = Class.forName("org.anti_ad.mc.ipnext.event.LockSlotsHandler");
                Object instance = clazz.getField("INSTANCE").get(null);
                Boolean slotLocked = (Boolean) clazz
                        .getMethod("isMappedSlotLocked", Slot.class)
                        .invoke(instance, slot);
                if (slotLocked) return true;
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                     | IllegalAccessException | NoSuchFieldException e) {
                StackToNearbyChests.LOGGER.warn(
                        "An exception occurred when determining whether the slot is locked by IPN mod", e);
            }
        }

        return LockedSlots.isLocked(slot);
    }
}