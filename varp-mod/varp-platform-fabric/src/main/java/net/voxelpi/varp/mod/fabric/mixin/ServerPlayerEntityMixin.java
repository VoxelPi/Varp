package net.voxelpi.varp.mod.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.portal.TeleportTransition;
import net.voxelpi.varp.mod.fabric.event.ServerEntityTeleportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(
        method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
        at = @At("HEAD")
    )
    public void teleport(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> callbackInfo) {
        ServerPlayer player = ((ServerPlayer) (Object) this);
        if (player.isRemoved()) {
            return;
        }

        // Get the start location.
        ServerLevel startWorld = player.level();
        PositionMoveRotation startPosition = PositionMoveRotation.of(player);

        // Get the end location.
        ServerLevel endWorld = transition.newLevel();
        PositionMoveRotation endPosition = PositionMoveRotation.calculateAbsolute(
            PositionMoveRotation.of(player),
            PositionMoveRotation.of(transition),
            transition.relatives()
        );

        // Fire the teleport event.
        ServerEntityTeleportEvents.BEFORE_PLAYER_TELEPORT.invoker().beforeTeleport(player, startWorld, startPosition, endWorld, endPosition);
    }
}
