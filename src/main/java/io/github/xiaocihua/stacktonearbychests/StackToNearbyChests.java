package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.compat.ModConfigScreenFactory;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static java.util.function.Predicate.not;

/**
 * Classe principale du mod.
 *
 * Règles NeoForge importantes :
 * - @Mod ne prend PAS de paramètre dist
 * - FMLClientSetupEvent → MOD bus (via IEventBus injecté dans le constructeur)
 * - Événements de jeu (screen, tick…) → NeoForge.EVENT_BUS
 * - ModList.get() disponible seulement après FMLClientSetupEvent
 */
@Mod(ModOptions.MOD_ID)
public class StackToNearbyChests {

    public static final Logger LOGGER = LogManager.getLogger("StackToNearbyChests");

    // Initialisés dans onClientSetup, pas au chargement de la classe
    public static boolean IS_IPN_MOD_LOADED                = false;
    public static boolean IS_EASY_SHULKER_BOXES_MOD_LOADED = false;

    public record Vec2i(int x, int y) {}

    static final ResourceLocation BUTTON_TEXTURES =
            ResourceLocation.fromNamespaceAndPath(ModOptions.MOD_ID, "widget/");

    public static Optional<PosUpdatableButtonWidget> currentStackToNearbyContainersButton = Optional.empty();

    /**
     * NeoForge injecte automatiquement l'IEventBus du mod dans le constructeur.
     */
    public StackToNearbyChests(IEventBus modEventBus) {
        // FMLClientSetupEvent est un événement de cycle de vie → MOD bus
        modEventBus.addListener(this::onClientSetup);
    }

    // ── Setup client (MOD bus) ────────────────────────────────────────────────────

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // ModList disponible ici
            IS_IPN_MOD_LOADED                = ModList.get().isLoaded("inventoryprofilesnext");
            IS_EASY_SHULKER_BOXES_MOD_LOADED = ModList.get().isLoaded("easyshulkerboxes");

            // Systèmes du mod
            KeySequence.init();
            LockedSlots.init();
            InventoryActions.init();
            EndWorldTickExecutor.init();
            ForEachContainerTask.init();

            // Événements de jeu → NeoForge.EVENT_BUS
            NeoForge.EVENT_BUS.addListener(StackToNearbyChests::onScreenInit);

            // Raccourcis clavier globaux
            ModOptions.get().keymap.stackToNearbyContainersKey
                    .registerNotOnScreen(InventoryActions::stackToNearbyContainers);
            ModOptions.get().keymap.restockFromNearbyContainersKey
                    .registerNotOnScreen(InventoryActions::restockFromNearbyContainers);
            ModOptions.get().keymap.openModOptionsScreenKey
                    .registerNotOnScreen(() ->
                            Minecraft.getInstance().setScreen(new ModOptionsScreen(new ModOptionsGui())));

            // Écran de config dans le menu Mods NeoForge
            ModConfigScreenFactory.register();
        });
    }

    // ── Événements de jeu (NeoForge.EVENT_BUS) ───────────────────────────────────

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || player.isSpectator()) return;

        ModOptions.Appearance app = ModOptions.get().appearance;
        boolean showTooltip = app.showButtonTooltip.booleanValue();

        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
            if (app.showTheButtonsOnTheCreativeInventoryScreen.booleanValue()) {
                addButtonsOnInventoryScreen((AbstractContainerScreen<?>) screen, showTooltip, app);
            }

            NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Post e) -> {
                if (e.getScreen() != screen) return;
                if (isTextFieldActive(screen) || isInventoryTabNotSelected(screen)) return;

                ModOptions.Keymap km = ModOptions.get().keymap;
                boolean triggered = false;
                Slot focused = ((HandledScreenAccessor) screen).getFocusedSlot();
                if (focused != null && focused.hasItem()) {
                    triggered = km.quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey
                            .testThenRun(() -> InventoryActions.stackToNearbyContainers(focused.getItem().getItem()));
                }
                if (!triggered) km.stackToNearbyContainersKey.testThenRun(InventoryActions::stackToNearbyContainers);
                km.restockFromNearbyContainersKey.testThenRun(InventoryActions::restockFromNearbyContainers);
            });

        } else if (isContainerScreen(screen)) {
            AbstractContainerMenu menu = ((AbstractContainerScreen<?>) screen).getMenu();

            if (app.showQuickStackButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((AbstractContainerScreen<?>) screen)
                        .setTextures(getButtonSprites("quick_stack_button"))
                        .setTooltip(showTooltip ? Component.translatable("stacktonearbychests.tooltip.quickStackButton") : null)
                        .setPosUpdater(p -> getAbsolutePos((HandledScreenAccessor) p, app.quickStackButtonPosX, app.quickStackButtonPosY))
                        .setPressAction(b -> InventoryActions.quickStack(menu))
                        .build();
            }

            if (app.showRestockButton.booleanValue()) {
                new PosUpdatableButtonWidget.Builder((AbstractContainerScreen<?>) screen)
                        .setTextures(getButtonSprites("restock_button"))
                        .setTooltip(showTooltip ? Component.translatable("stacktonearbychests.tooltip.restockButton") : null)
                        .setPosUpdater(p -> getAbsolutePos((HandledScreenAccessor) p, app.restockButtonPosX, app.restockButtonPosY))
                        .setPressAction(b -> InventoryActions.restock(menu))
                        .build();
            }

            NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Post e) -> {
                if (e.getScreen() != screen) return;
                if (isTextFieldActive(screen) || isInventoryTabNotSelected(screen)) return;
                ModOptions.get().keymap.quickStackKey.testThenRun(() -> InventoryActions.quickStack(menu));
                ModOptions.get().keymap.restockKey.testThenRun(() -> InventoryActions.restock(menu));
            });
        }
    }

    private static void addButtonsOnInventoryScreen(AbstractContainerScreen<?> screen,
                                                    boolean showTooltip,
                                                    ModOptions.Appearance app) {
        if (app.showStackToNearbyContainersButton.booleanValue()) {
            var btn = new PosUpdatableButtonWidget.Builder(screen)
                    .setTextures(getButtonSprites("quick_stack_to_nearby_containers_button"))
                    .setTooltip(showTooltip ? getTooltipWithHint("stacktonearbychests.tooltip.stackToNearbyContainersButton") : null)
                    .setPosUpdater(p -> new Vec2i(
                            p.getX() + app.stackToNearbyContainersButtonPosX.intValue(),
                            p.getY() + app.stackToNearbyContainersButtonPosY.intValue()))
                    .setPressAction(b -> {
                        AbstractContainerMenu menu = screen.getMenu();
                        ItemStack cursor = menu.getCarried();
                        if (cursor.isEmpty()) {
                            InventoryActions.stackToNearbyContainers();
                        } else {
                            Item item = cursor.getItem();
                            menu.slots.stream()
                                    .filter(s -> s.container instanceof Inventory)
                                    .filter(s -> s.getContainerSlot() < 36)
                                    .filter(not(LockedSlots::isLocked))
                                    .filter(s -> !s.hasItem() || InventoryActions.canMerge(s.getItem(), cursor))
                                    .peek(s -> InventoryActions.pickup(menu, s))
                                    .anyMatch(s -> cursor.isEmpty());
                            InventoryActions.stackToNearbyContainers(item);
                        }
                    })
                    .build();

            currentStackToNearbyContainersButton = Optional.ofNullable(btn);
            NeoForge.EVENT_BUS.addListener((ScreenEvent.Closing e) -> {
                if (e.getScreen() == screen) currentStackToNearbyContainersButton = Optional.empty();
            });
        }

        if (app.showRestockFromNearbyContainersButton.booleanValue()) {
            new PosUpdatableButtonWidget.Builder(screen)
                    .setTextures(getButtonSprites("restock_from_nearby_containers_button"))
                    .setTooltip(showTooltip ? getTooltipWithHint("stacktonearbychests.tooltip.restockFromNearbyContainersButton") : null)
                    .setPosUpdater(p -> new Vec2i(
                            p.getX() + app.restockFromNearbyContainersButtonPosX.intValue(),
                            p.getY() + app.restockFromNearbyContainersButtonPosY.intValue()))
                    .setPressAction(b -> InventoryActions.restockFromNearbyContainers())
                    .build();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static WidgetSprites getButtonSprites(String name) {
        return new WidgetSprites(
                BUTTON_TEXTURES.withSuffix(name + ".png"),
                BUTTON_TEXTURES.withSuffix(name + "_highlighted.png"));
    }

    private static boolean isTextFieldActive(Screen screen) {
        var focused = screen.getFocused();
        if (focused instanceof RecipeBookComponent rb) {
            EditBox sf = ((RecipeBookWidgetAccessor) rb).getSearchField();
            if (sf != null && sf.isActive()) return true;
        }
        return focused instanceof EditBox eb && eb.isActive();
    }

    private static boolean isInventoryTabNotSelected(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen c && !c.isInventoryOpen();
    }

    public static Vec2i getAbsolutePos(HandledScreenAccessor p, ModOptions.IntOption x, ModOptions.IntOption y) {
        return new Vec2i(
                p.getX() + p.getBackgroundWidth()      + x.intValue(),
                p.getY() + p.getBackgroundHeight() / 2 + y.intValue());
    }

    private static Component getTooltipWithHint(String key) {
        return Component.translatable(key)
                .append("\n")
                .append(Component.translatable("stacktonearbychests.tooltip.hint")
                        .setStyle(Style.EMPTY.withItalic(true).withColor(net.minecraft.ChatFormatting.DARK_GRAY)));
    }

    public static boolean isContainerScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        if (screen instanceof BeaconScreen || screen instanceof GrindstoneScreen
                || screen instanceof CartographyTableScreen || screen instanceof CraftingScreen
                || screen instanceof LoomScreen || screen instanceof EnchantmentScreen
                || screen instanceof MerchantScreen || screen instanceof AbstractFurnaceScreen<?>
                || screen instanceof StonecutterScreen || screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        if (screen instanceof HorseInventoryScreen hs) {
            return ((MountScreenAccessor) hs).getMount() instanceof AbstractChestedHorse h && h.hasChest();
        }
        return true;
    }
}