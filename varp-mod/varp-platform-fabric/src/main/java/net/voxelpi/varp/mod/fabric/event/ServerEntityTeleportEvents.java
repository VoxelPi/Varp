package net.voxelpi.varp.mod.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;

public final class ServerEntityTeleportEvents {

    /**
     * An event that is called before a player is teleported.
     */
    public static final Event<BeforePlayerTeleport> BEFORE_PLAYER_TELEPORT = EventFactory.createArrayBacked(BeforePlayerTeleport.class, callbacks -> (player, fromWorld, fromPosition, toWorld, toPosition) -> {
        for (BeforePlayerTeleport callback : callbacks) {
            callback.beforeTeleport(player, fromWorld, fromPosition, toWorld, toPosition);
        }
    });

    @FunctionalInterface
    public interface BeforePlayerTeleport {
        /**
         * Called before a player is teleported.
         *
         * @param player the player.
         * @param fromWorld the world the player is currently in.
         * @param fromPosition the players current position.
         * @param toWorld the world the player is teleported in.
         * @param toPosition the position the player teleports to.
         */
        void beforeTeleport(ServerPlayer player, ServerLevel fromWorld, PositionMoveRotation fromPosition, ServerLevel toWorld, PositionMoveRotation toPosition);
    }
}
