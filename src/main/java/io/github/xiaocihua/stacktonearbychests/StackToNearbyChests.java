package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsScreen;
import io.github.xiaocihua.stacktonearbychests.gui.PosUpdatableButtonWidget;
import io.github.xiaocihua.stacktonearbychests.mixin.HandledScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.MountScreenAccessor;
import io.github.xiaocihua.stacktonearbychests.mixin.RecipeBookWidgetAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static java.util.function.Predicate.not;

@Mod(value = ModOptions.MOD_ID, dist = Dist.CLIENT)
public class StackToNearbyChests {

    public static final Logger LOGGER = LogManager.getLogger("StackToNearbyChests");

    public static final boolean IS_IPN_MOD_LOADED = ModList.get().isLoaded("inventoryprofilesnext");
    public static final boolean IS_EASY_SHULKER_BOXES_MOD_LOADED = ModList.get().isLoaded("easyshulkerboxes");

    public record Vec2i(int x, int y) {}

    static final ResourceLocation BUTTON_TEXTURES =
            ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "widget/");

    public static Optional<PosUpdatableButtonWidget> currentStackToNearbyContainersButton = Optional.empty();

    public StackToNearbyChests() {
        // IMPORTANT : aucune logique client ici
        EndWorldTickExecutor.init();
        ForEachContainerTask.init();

        NeoForge.EVENT_BUS.register(this);
    }

    // -------------------------------------------------------------------------
    // CLIENT SETUP — tout le code client doit être ici
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            KeySequence.init();
            LockedSlots.init();
            InventoryActions.init();

            ModOptions.get().keymap.stackToNearbyContainersKey
                    .registerNotOnScreen(InventoryActions::stackToNearbyContainers);

            ModOptions.get().keymap.restockFromNearbyContainersKey
                    .registerNotOnScreen(InventoryActions::restockFromNearbyContainers);

            ModOptions.get().keymap.openModOptionsScreenKey
                    .registerNotOnScreen(() ->
                            Minecraft.getInstance().setScreen(new ModOptionsScreen(new ModOptionsGui())));
        });
    }

    // -------------------------------------------------------------------------
    // SCREEN INIT — remplace ScreenEvents.AFTER_INIT
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        addButtonsAndKeys(event);
    }

    // -------------------------------------------------------------------------
    // LOGIQUE D’AJOUT DES BOUTONS
    // -------------------------------------------------------------------------
    private void addButtonsAndKeys(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || player.isSpectator()) return;

        ModOptions.Appearance appearanceOption = ModOptions.get().appearance;
        boolean showButtonTooltip = appearanceOption.showButtonTooltip.booleanValue();

        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {

            if (appearanceOption.showTheButtonsOnTheCreativeInventoryScreen.booleanValue()) {
                addButtonsOnInventoryScreen((AbstractContainerScreen<?>) screen, showButtonTooltip, appearanceOption);
            }

            NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Post keyEvent) -> {
                if (keyEvent.getScreen() != screen) return;
                if (isTextFieldActive(screen) || isInventoryTabNotSelected(screen)) return;

                ModOptions.Keymap keymap = ModOptions.get().keymap;
                boolean triggered = false;

                Slot focusedSlot = ((HandledScreenAccessor) screen).getFocusedSlot();
                if (focusedSlot != null && focusedSlot.hasItem()) {
                    triggered = keymap.quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey
                            .testThenRun(() -> InventoryActions.stackToNearbyContainers(focusedSlot.getItem().getItem()));
                }

                if (!triggered) {
                    keymap.stackToNearbyContainersKey.testThenRun(InventoryActions::stackToNearbyContainers);
                }

                keymap.restockFromNearbyContainersKey.testThenRun(InventoryActions::restockFromNearbyContainers);
            });

        } else if (isContainerScreen(screen)) {
            AbstractContainerMenu menu = ((AbstractContainerScreen<?>) screen).getMenu();

            if (ModOptions.get().appearance.showQuickStackButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((AbstractContainerScreen<?>) screen)
                        .setTextures(getButtonSprites("quick_stack_button"))
                        .setTooltip(showButtonTooltip
                                ? Component.translatable("stacktonearbychests.tooltip.quickStackButton") : null)
                        .setPosUpdater(parent -> getAbsolutePos(
                                (HandledScreenAccessor) parent,
                                appearanceOption.quickStackButtonPosX,
                                appearanceOption.quickStackButtonPosY))
                        .setPressAction(button -> InventoryActions.quickStack(menu))
                        .build();
            }

            if (ModOptions.get().appearance.showRestockButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((AbstractContainerScreen<?>) screen)
                        .setTextures(getButtonSprites("restock_button"))
                        .setTooltip(showButtonTooltip
                                ? Component.translatable("stacktonearbychests.tooltip.restockButton") : null)
                        .setPosUpdater(parent -> getAbsolutePos(
                                (HandledScreenAccessor) parent,
                                appearanceOption.restockButtonPosX,
                                appearanceOption.restockButtonPosY))
                        .setPressAction(button -> InventoryActions.restock(menu))
                        .build();
            }

            NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Post keyEvent) -> {
                if (keyEvent.getScreen() != screen) return;
                if (isTextFieldActive(screen) || isInventoryTabNotSelected(screen)) return;

                ModOptions.get().keymap.quickStackKey.testThenRun(() -> InventoryActions.quickStack(menu));
                ModOptions.get().keymap.restockKey.testThenRun(() -> InventoryActions.restock(menu));
            });
        }
    }

    // -------------------------------------------------------------------------
    // BOUTONS INVENTAIRE
    // -------------------------------------------------------------------------
    private static void addButtonsOnInventoryScreen(
            AbstractContainerScreen<?> screen,
            boolean showButtonTooltip,
            ModOptions.Appearance appearanceOption) {

        if (ModOptions.get().appearance.showStackToNearbyContainersButton.booleanValue()) {
            var buttonWidget = new PosUpdatableButtonWidget.Builder(screen)
                    .setTextures(getButtonSprites("quick_stack_to_nearby_containers_button"))
                    .setTooltip(showButtonTooltip
                            ? getTooltipWithHint("stacktonearbychests.tooltip.stackToNearbyContainersButton") : null)
                    .setPosUpdater(parent -> new Vec2i(
                            parent.getX() + appearanceOption.stackToNearbyContainersButtonPosX.intValue(),
                            parent.getY() + appearanceOption.stackToNearbyContainersButtonPosY.intValue()))
                    .setPressAction(button -> {
                        AbstractContainerMenu menu = screen.getMenu();
                        ItemStack cursorStack = menu.getCarried();
                        if (cursorStack.isEmpty()) {
                            InventoryActions.stackToNearbyContainers();
                        } else {
                            Item item = cursorStack.getItem();

                            menu.slots.stream()
                                    .filter(slot -> slot.container instanceof Inventory)
                                    .filter(slot -> slot.getContainerSlot() < 36)
                                    .filter(not(LockedSlots::isLocked))
                                    .filter(slot -> !slot.hasItem() || InventoryActions.canMerge(slot.getItem(), cursorStack))
                                    .peek(slot -> InventoryActions.pickup(menu, slot))
                                    .anyMatch(slot -> cursorStack.isEmpty());

                            InventoryActions.stackToNearbyContainers(item);
                        }
                    })
                    .build();

            currentStackToNearbyContainersButton = Optional.ofNullable(buttonWidget);

            NeoForge.EVENT_BUS.addListener((ScreenEvent.Closing closeEvent) -> {
                if (closeEvent.getScreen() == screen) {
                    currentStackToNearbyContainersButton = Optional.empty();
                }
            });
        }

        if (ModOptions.get().appearance.showRestockFromNearbyContainersButton.booleanValue()) {
            new PosUpdatableButtonWidget.Builder(screen)
                    .setTextures(getButtonSprites("restock_from_nearby_containers_button"))
                    .setTooltip(showButtonTooltip
                            ? getTooltipWithHint("stacktonearbychests.tooltip.restockFromNearbyContainersButton") : null)
                    .setPosUpdater(parent -> new Vec2i(
                            parent.getX() + appearanceOption.restockFromNearbyContainersButtonPosX.intValue(),
                            parent.getY() + appearanceOption.restockFromNearbyContainersButtonPosY.intValue()))
                    .setPressAction(button -> InventoryActions.restockFromNearbyContainers())
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------
    private static WidgetSprites getButtonSprites(String name) {
        ResourceLocation normal = BUTTON_TEXTURES.withSuffix(name + ".png");
        ResourceLocation hover  = BUTTON_TEXTURES.withSuffix(name + "_highlighted.png");
        return new WidgetSprites(normal, hover);
    }

    private static boolean isTextFieldActive(Screen screen) {
        var focused = screen.getFocused();

        if (focused instanceof RecipeBookComponent recipeBook) {
            EditBox searchField = ((RecipeBookWidgetAccessor) recipeBook).getSearchField();
            if (searchField != null && searchField.isActive())
                return true;
        }

        return focused instanceof EditBox editBox && editBox.isActive();
    }

    private static boolean isInventoryTabNotSelected(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen creative
                && !creative.isInventoryOpen();
    }

    public static Vec2i getAbsolutePos(HandledScreenAccessor parent, ModOptions.IntOption x, ModOptions.IntOption y) {
        return new Vec2i(
                parent.getX() + parent.getBackgroundWidth() + x.intValue(),
                parent.getY() + parent.getBackgroundHeight() / 2 + y.intValue());
    }

    private static Component getTooltipWithHint(String translationKey) {
        return Component.translatable(translationKey)
                .append("\n")
                .append(Component.translatable("stacktonearbychests.tooltip.hint")
                        .setStyle(Style.EMPTY.withItalic(true)
                                .withColor(net.minecraft.ChatFormatting.DARK_GRAY)));
    }

    public static boolean isContainerScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>))
            return false;

        if (screen instanceof BeaconScreen
                || screen instanceof GrindstoneScreen
                || screen instanceof CartographyTableScreen
                || screen instanceof CraftingScreen
                || screen instanceof LoomScreen
                || screen instanceof EnchantmentScreen
                || screen instanceof MerchantScreen
                || screen instanceof AbstractFurnaceScreen<?>
                || screen instanceof StonecutterScreen
                || screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen) {
            return false;
        }

        if (screen instanceof HorseInventoryScreen mountScreen){
            return ((MountScreenAccessor) mountScreen).getMount() instanceof AbstractChestedHorse horse
                    && horse.hasChest();
        }

        return true;
    }
}
