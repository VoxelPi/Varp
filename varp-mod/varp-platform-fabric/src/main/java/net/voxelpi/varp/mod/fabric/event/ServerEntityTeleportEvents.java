package net.voxelpi.varp.mod.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EntityPosition;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

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
        void beforeTeleport(ServerPlayerEntity player, ServerWorld fromWorld, EntityPosition fromPosition, ServerWorld toWorld, EntityPosition toPosition);
    }
}
