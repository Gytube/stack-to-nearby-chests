package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ItemEntry extends SelectableEntryList.Entry<ResourceLocation> {

    private final Optional<Item> item;
    private final Component name;

    public ItemEntry(ResourceLocation id) {
        super(id);
        item = BuiltInRegistries.ITEM.getOptional(id);
        name = item.map(Item::getDescription).orElse(Component.literal(id.toString()));
    }

    // ✅ fix: PAS de super.render()
    @Override
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        var font     = Minecraft.getInstance().font;
        int inset    = 6;
        int iconSize = 16;

        item.ifPresent(i -> graphics.renderItem(
                new ItemStack(i), x + inset, y + (height - iconSize) / 2));

        int fontWidth = font.width(name);
        graphics.drawString(font, name,
                x + width - inset - fontWidth, y + (height - font.lineHeight) / 2,
                ModOptionsGui.TEXT_COLOR, false);
    }
}