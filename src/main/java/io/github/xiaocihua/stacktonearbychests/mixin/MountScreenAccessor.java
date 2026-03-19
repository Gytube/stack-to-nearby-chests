package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Remplace MountScreen → AbstractHorseScreen (Mojang mappings)
@Mixin(HorseInventoryScreen.class)
public interface MountScreenAccessor {

    // Remplace "mount" → "horse" (Mojang mappings)
    @Accessor("horse")
    LivingEntity getMount();
}