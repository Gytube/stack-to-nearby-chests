package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class BlockContainerEntry extends SelectableEntryList.Entry<ResourceLocation> {

    private final Optional<Block> block;
    private final Component name;

    public BlockContainerEntry(ResourceLocation id) {
        super(id);
        block = BuiltInRegistries.BLOCK.getOptional(id);
        name  = block.<Component>map(Block::getName).orElse(Component.literal(id.toString()));
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        this.render(graphics, x, y, mouseX, mouseY);
        var font   = Minecraft.getInstance().font;
        int inset  = 6;
        int iconSize = 16;

        // Icône de l'item correspondant au bloc
        block.ifPresent(b -> graphics.renderItem(
                new net.minecraft.world.item.ItemStack(b.asItem()),
                x + inset,
                y + (height - iconSize) / 2));

        // Nom
        int fontWidth = font.width(name);
        int fontY     = y + (height - font.lineHeight) / 2;
        graphics.drawString(font, name, x + width - inset - fontWidth, fontY, ModOptionsGui.TEXT_COLOR, false);
    }
}