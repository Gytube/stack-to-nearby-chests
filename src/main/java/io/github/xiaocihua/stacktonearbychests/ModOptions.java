package io.github.xiaocihua.stacktonearbychests;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static io.github.xiaocihua.stacktonearbychests.StackToNearbyChests.LOGGER;

@OnlyIn(Dist.CLIENT)
public class ModOptions {
    // Remplace "stack-to-nearby-chests" — tirets interdits dans les mod IDs NeoForge
    public static final String MOD_ID = "stacktonearbychests";

    // Remplace FabricLoader.getInstance().getConfigDir()
    public static final Path MOD_OPTIONS_DIR = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
    public static final Path OPTIONS_FILE = MOD_OPTIONS_DIR.resolve("mod-options.json");

    private static final ModOptions options = read();

    public Appearance appearance = new Appearance();
    public Behavior behavior = new Behavior();
    public Keymap keymap = new Keymap();

    public static ModOptions get() {
        return options;
    }

    public static ModOptions getDefault() {
        return new ModOptions();
    }

    private static ModOptions read() {
        try (BufferedReader reader = Files.newBufferedReader(OPTIONS_FILE, StandardCharsets.UTF_8)) {
            return new GsonBuilder()
                    .registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter().nullSafe())
                    .create()
                    .fromJson(reader, ModOptions.class);
        } catch (NoSuchFileException e) {
            LOGGER.info("Options file does not exist, creating a new one");
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.info("Failed to read options file, creating a new one", e);
        }

        ModOptions modOptions = getDefault();
        modOptions.write();
        return modOptions;
    }

    public void write() {
        try {
            Files.createDirectories(OPTIONS_FILE.getParent());
            String json = new GsonBuilder()
                    .registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter().nullSafe())
                    .setPrettyPrinting()
                    .create()
                    .toJson(this);
            Files.writeString(OPTIONS_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to write options file", e);
        }
    }

    public static class Appearance {
        public MutableBoolean showStackToNearbyContainersButton = new MutableBoolean(true);
        public MutableBoolean showRestockFromNearbyContainersButton = new MutableBoolean(true);
        public MutableBoolean showQuickStackButton = new MutableBoolean(true);
        public MutableBoolean showRestockButton = new MutableBoolean(true);

        public MutableBoolean showButtonTooltip = new MutableBoolean(true);

        public IntOption stackToNearbyContainersButtonPosX = new IntOption(140);
        public IntOption stackToNearbyContainersButtonPosY = new IntOption(170);

        public IntOption restockFromNearbyContainersButtonPosX = new IntOption(160);
        public IntOption restockFromNearbyContainersButtonPosY  = new IntOption(170);

        public IntOption quickStackButtonPosX = new IntOption(6);
        public IntOption quickStackButtonPosY = new IntOption(-10);

        public IntOption restockButtonPosX = new IntOption(6);
        public IntOption restockButtonPosY = new IntOption(10);

        // Remplace Identifier.of() → ResourceLocation.fromNamespaceAndPath()
        public ResourceLocation favoriteItemStyle = ResourceLocation.fromNamespaceAndPath(MOD_ID, "gold_badge");

        public MutableBoolean alwaysShowMarkersForFavoritedItems = new MutableBoolean(true);

        public MutableBoolean showTheButtonsOnTheCreativeInventoryScreen = new MutableBoolean(true);

        public MutableBoolean enableFavoritingSoundEffect = new MutableBoolean(true);
    }

    public static class Behavior {
        public IntOption searchInterval = new IntOption(0);

        public MutableBoolean supportForContainerEntities = new MutableBoolean(true);

        public MutableBoolean doNotQuickStackItemsFromTheHotbar = new MutableBoolean(false);

        public MutableBoolean enableItemFavoriting = new MutableBoolean(true);
        public MutableBoolean favoriteItemsCannotBePickedUp = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeThrown = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeQuickMoved = new MutableBoolean(false);
        public MutableBoolean favoriteItemStacksCannotBeSwapped = new MutableBoolean(false);
        public MutableBoolean favoriteItemsCannotBeSwappedWithOffhand = new MutableBoolean(false);

        public Set<String> stackingTargets = Set.of("minecraft:shulker_box",
                "minecraft:brown_shulker_box", "minecraft:yellow_shulker_box",
                "minecraft:green_shulker_box", "minecraft:purple_shulker_box",
                "minecraft:barrel", "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box", "minecraft:trapped_chest",
                "minecraft:ender_chest", "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box", "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box", "minecraft:chest",
                "minecraft:blue_shulker_box", "minecraft:magenta_shulker_box",
                "minecraft:cyan_shulker_box", "minecraft:pink_shulker_box",
                "minecraft:red_shulker_box", "minecraft:light_blue_shulker_box",
                "ironchests:diamond_chest", "ironchests:iron_chest",
                "ironchests:gold_chest", "ironchests:crystal_chest",
                "ironchests:copper_chest", "ironchests:dirt_chest",
                "ironchests:obsidian_chest", "ironchests:netherite_chest",
                "reinfchest:copper_chest", "reinfchest:iron_chest",
                "reinfchest:gold_chest", "reinfchest:diamond_chest",
                "reinfchest:netherite_chest", "reinfbarrel:copper_barrel",
                "reinfbarrel:iron_barrel", "reinfbarrel:gold_barrel",
                "reinfbarrel:diamond_barrel", "reinfbarrel:netherite_barrel");

        public Set<String> stackingTargetEntities = Set.of(
                "minecraft:oak_chest_boat", "minecraft:spruce_chest_boat",
                "minecraft:birch_chest_boat", "minecraft:jungle_chest_boat",
                "minecraft:acacia_chest_boat", "minecraft:dark_oak_chest_boat",
                "minecraft:mangrove_chest_boat", "minecraft:cherry_chest_boat",
                "minecraft:bamboo_chest_raft", "minecraft:trader_llama",
                "minecraft:chest_minecart", "minecraft:donkey",
                "minecraft:llama", "minecraft:mule");

        public Set<String> itemsThatWillNotBeStacked = Set.of(
                "minecraft:shulker_box", "minecraft:brown_shulker_box",
                "minecraft:yellow_shulker_box", "minecraft:green_shulker_box",
                "minecraft:purple_shulker_box", "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box", "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box", "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box", "minecraft:blue_shulker_box",
                "minecraft:magenta_shulker_box", "minecraft:cyan_shulker_box",
                "minecraft:pink_shulker_box", "minecraft:red_shulker_box",
                "minecraft:light_blue_shulker_box", "minecraft:bundle");

        public Set<String> restockingSources = Set.of("minecraft:shulker_box",
                "minecraft:brown_shulker_box", "minecraft:yellow_shulker_box",
                "minecraft:green_shulker_box", "minecraft:purple_shulker_box",
                "minecraft:barrel", "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box", "minecraft:trapped_chest",
                "minecraft:ender_chest", "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box", "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box", "minecraft:chest",
                "minecraft:blue_shulker_box", "minecraft:magenta_shulker_box",
                "minecraft:cyan_shulker_box", "minecraft:pink_shulker_box",
                "minecraft:red_shulker_box", "minecraft:light_blue_shulker_box",
                "ironchests:diamond_chest", "ironchests:iron_chest",
                "ironchests:gold_chest", "ironchests:crystal_chest",
                "ironchests:copper_chest", "ironchests:dirt_chest",
                "ironchests:obsidian_chest", "ironchests:netherite_chest",
                "reinfchest:copper_chest", "reinfchest:iron_chest",
                "reinfchest:gold_chest", "reinfchest:diamond_chest",
                "reinfchest:netherite_chest", "reinfbarrel:copper_barrel",
                "reinfbarrel:iron_barrel", "reinfbarrel:gold_barrel",
                "reinfbarrel:diamond_barrel", "reinfbarrel:netherite_barrel");

        public Set<String> restockingSourceEntities = Set.of(
                "minecraft:oak_chest_boat", "minecraft:spruce_chest_boat",
                "minecraft:birch_chest_boat", "minecraft:jungle_chest_boat",
                "minecraft:acacia_chest_boat", "minecraft:dark_oak_chest_boat",
                "minecraft:mangrove_chest_boat", "minecraft:cherry_chest_boat",
                "minecraft:bamboo_chest_raft", "minecraft:trader_llama",
                "minecraft:chest_minecart", "minecraft:donkey",
                "minecraft:llama", "minecraft:mule");

        public Set<String> itemsThatWillNotBeRestocked = Set.of(
                "minecraft:shulker_box", "minecraft:brown_shulker_box",
                "minecraft:yellow_shulker_box", "minecraft:green_shulker_box",
                "minecraft:purple_shulker_box", "minecraft:orange_shulker_box",
                "minecraft:light_gray_shulker_box", "minecraft:black_shulker_box",
                "minecraft:lime_shulker_box", "minecraft:white_shulker_box",
                "minecraft:gray_shulker_box", "minecraft:blue_shulker_box",
                "minecraft:magenta_shulker_box", "minecraft:cyan_shulker_box",
                "minecraft:pink_shulker_box", "minecraft:red_shulker_box",
                "minecraft:light_blue_shulker_box", "minecraft:bundle");
    }

    public static class Keymap {
        public KeySequence stackToNearbyContainersKey = KeySequence.empty();
        public KeySequence quickStackItemsOfTheSameTypeAsTheOneUnderTheCursorToNearbyContainersKey = KeySequence.empty();
        public KeySequence restockFromNearbyContainersKey = KeySequence.empty();
        public KeySequence quickStackKey = KeySequence.empty();
        public KeySequence restockKey = KeySequence.empty();
        public KeySequence showMarkersForFavoritedItemsKey = new KeySequence(List.of(GLFW.GLFW_KEY_LEFT_ALT));
        public KeySequence markAsFavoriteKey = new KeySequence(List.of(
                GLFW.GLFW_KEY_LEFT_ALT,
                GLFW.GLFW_MOUSE_BUTTON_2 - KeySequence.MOUSE_BUTTON_CODE_OFFSET));
        public KeySequence openModOptionsScreenKey = new KeySequence(List.of(
                GLFW.GLFW_KEY_LEFT_CONTROL,
                GLFW.GLFW_KEY_S,
                GLFW.GLFW_KEY_C));
    }

    public static class IntOption extends MutableInt {
        private final int defaultValue;

        public IntOption(int value) {
            super(value);
            defaultValue = value;
        }

        public int reset() {
            setValue(defaultValue);
            return defaultValue;
        }
    }

    // Remplace IdentifierAdapter → ResourceLocationAdapter
    public static class ResourceLocationAdapter extends TypeAdapter<ResourceLocation> {

        @Override
        public ResourceLocation read(JsonReader in) throws IOException {
            return ResourceLocation.parse(in.nextString());
        }

        @Override
        public void write(JsonWriter out, ResourceLocation value) throws IOException {
            out.value(value.toString());
        }
    }
}