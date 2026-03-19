package io.github.xiaocihua.stacktonearbychests.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntityContainerEntry extends SelectableEntryList.Entry<ResourceLocation> {

    private final Component name;

    public EntityContainerEntry(ResourceLocation id) {
        super(id);
        name = BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .map(EntityType::getDescription)
                .orElse(Component.literal(id.toString()));
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        this.render(graphics, x, y, mouseX, mouseY);
        var font      = Minecraft.getInstance().font;
        int inset     = 6;
        int fontWidth = font.width(name);
        int fontY     = y + (height - font.lineHeight) / 2;
        graphics.drawString(font, name, x + width - inset - fontWidth, fontY, ModOptionsGui.TEXT_COLOR, false);
    }
}