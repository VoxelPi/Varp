package net.voxelpi.varp.mod.server.api

import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.repository.compositor.TreeCompositor
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the server side varp implementation.
 */
public interface VarpServerAPI {

    public val version: String

    public val loader: VarpLoader

    public val compositor: TreeCompositor
        get() = loader.compositor

    public val tree: Tree
        get() = loader.tree

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
