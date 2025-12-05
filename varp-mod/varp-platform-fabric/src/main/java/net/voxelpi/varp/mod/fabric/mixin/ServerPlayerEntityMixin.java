package net.voxelpi.varp.mod.fabric.mixin;

import net.minecraft.entity.EntityPosition;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.voxelpi.varp.mod.fabric.event.ServerEntityTeleportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(
        method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;",
        at = @At("HEAD")
    )
    public void teleportTo(TeleportTarget teleportTarget, CallbackInfoReturnable<ServerPlayerEntity> callbackInfo) {
        ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
        if (player.isRemoved()) {
            return;
        }

        // Get the start location.
        ServerWorld startWorld = player.getEntityWorld();
        EntityPosition startPosition = EntityPosition.fromEntity(player);

        // Get the end location.
        ServerWorld endWorld = teleportTarget.world();
        EntityPosition endPosition = EntityPosition.apply(
            EntityPosition.fromEntity(player),
            EntityPosition.fromTeleportTarget(teleportTarget),
            teleportTarget.relatives()
        );

        // Fire the teleport event.
        ServerEntityTeleportEvents.BEFORE_PLAYER_TELEPORT.invoker().beforeTeleport(player, startWorld, startPosition, endWorld, endPosition);
    }
}
