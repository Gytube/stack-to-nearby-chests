package io.github.xiaocihua.stacktonearbychests.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    // Remplace LevelStorage.Session → LevelStorageSource.LevelStorageAccess (Mojang mappings)
    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess getSession();
}