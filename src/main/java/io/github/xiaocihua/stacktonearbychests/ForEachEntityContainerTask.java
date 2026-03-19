package io.github.xiaocihua.stacktonearbychests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import static io.github.xiaocihua.stacktonearbychests.MathUtil.getBox;

public class ForEachEntityContainerTask extends ForEachContainerTask {

    private final MultiPlayerGameMode interactionManager;
    private final double squaredReachDistance;
    private final Entity cameraEntity;

    private final Iterator<Entity> entities;

    public ForEachEntityContainerTask(Minecraft client,
                                      LocalPlayer player,
                                      Consumer<AbstractContainerMenu> action,
                                      Entity cameraEntity,
                                      Level world,
                                      MultiPlayerGameMode interactionManager,
                                      Collection<String> filter) {
        super(client, player, action);
        this.interactionManager = interactionManager;
        double reachDistance = player.entityInteractionRange();
        this.squaredReachDistance = Mth.square(reachDistance);
        this.cameraEntity = cameraEntity;

        Vec3 eyePos = cameraEntity.getEyePosition(0);

        // Remplace EntityPredicates.VALID_INVENTORIES + RideableInventory + Registries → BuiltInRegistries
        Iterator<Entity> allEntities = world.getEntities(
                cameraEntity,
                getBox(eyePos, reachDistance),
                entity -> !(entity instanceof Player)
                        && entity.isAlive()
                        && filter.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString())
                        && (!(entity instanceof AbstractChestedHorse horse) || horse.hasChest())
        ).iterator();

        this.entities = allEntities;
    }

    @Override
    public void start() {
        if (!entities.hasNext()) {
            super.stop();
            return;
        }
        // Remplace client.options.sneakKey.setPressed(true)
        client.options.keyShift.setDown(true);
        EndWorldTickExecutor.execute(super::start);
    }

    @Override
    protected void stop() {
        client.options.keyShift.setDown(false);
        EndWorldTickExecutor.execute(super::stop);
    }

    @Override
    protected boolean findAndOpenNextContainer() {
        Vec3 eyePos = cameraEntity.getEyePosition(0);

        while (entities.hasNext()) {
            Entity entity = entities.next();

            if (entity.distanceToSqr(eyePos) > squaredReachDistance) continue;

            // Remplace interactionManager.interactEntity → interactAt
            interactionManager.interactAt(player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND);

            return true;
        }

        return false;
    }
}