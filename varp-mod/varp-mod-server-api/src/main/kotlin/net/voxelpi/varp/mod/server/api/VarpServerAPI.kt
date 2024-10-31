package net.voxelpi.varp.mod.server.api

import net.voxelpi.event.EventScope
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayerService
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.repository.compositor.Compositor
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the server side varp implementation.
 */
public interface VarpServerAPI {

    /**
     * The version of the server varp mod.
     */
    public val version: String

    /**
     * The server event service.
     */
    public val eventScope: EventScope

    /**
     * The platform on which the server implementation runs.
     */
    public val platform: ServerPlatform

    /**
     * The varp loader used to load and store the varp tree.
     */
    public val loader: VarpLoader

    /**
     * The varp compositor used to composite the varp tree.
     */
    public val compositor: Compositor
        get() = loader.compositor

    /**
     * The varp tree.
     */
    public val tree: Tree
        get() = loader.tree

    /**
     * The server player service.
     */
    public val playerService: VarpServerPlayerService

    public companion object {
        private var provider: VarpServerAPI? = null

        /**
         * Returns the currently loaded api implementation.
         */
        public fun get(): VarpServerAPI {
            return provider ?: throw IllegalStateException("No implementation of the server varp api is loaded.")
        }

        @Internal
        public fun register(provider: VarpServerAPI) {
            this.provider = provider
        }

        @Internal
        public fun unregister() {
            this.provider = null
        }
    }
}
