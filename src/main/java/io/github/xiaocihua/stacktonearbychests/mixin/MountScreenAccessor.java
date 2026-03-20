package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// AbstractHorseScreen n'est pas accessible en 1.21.1 → on cible HorseInventoryScreen
@Mixin(HorseInventoryScreen.class)
public interface MountScreenAccessor {

    @Accessor("horse")
    LivingEntity getMount();
}