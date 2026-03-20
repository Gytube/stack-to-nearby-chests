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

    // ✅ fix: PAS de super.render() — la méthode parente est abstraite.
    // Le fond de sélection est dessiné par SelectableEntryList.Entry.render() (la méthode final)
    // avant d'appeler cette méthode-ci.
    @Override
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        var font     = Minecraft.getInstance().font;
        int inset    = 6;
        int iconSize = 16;

        block.ifPresent(b -> graphics.renderItem(
                new net.minecraft.world.item.ItemStack(b.asItem()),
                x + inset, y + (height - iconSize) / 2));

        int fontWidth = font.width(name);
        graphics.drawString(font, name,
                x + width - inset - fontWidth, y + (height - font.lineHeight) / 2,
                ModOptionsGui.TEXT_COLOR, false);
    }
}