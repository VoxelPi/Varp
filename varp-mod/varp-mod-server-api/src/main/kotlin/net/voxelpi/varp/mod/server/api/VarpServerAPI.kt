package net.voxelpi.varp.mod.server.api

import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.repository.compositor.TreeCompositor
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the server side varp implementation.
 */
interface VarpServerAPI {

    val version: String

    val loader: VarpLoader

    val compositor: TreeCompositor
        get() = loader.compositor

    val tree: Tree
        get() = loader.tree

    companion object {
        private var provider: VarpServerAPI? = null

        /**
         * Returns the currently loaded api implementation.
         */
        fun get(): VarpServerAPI {
            return provider ?: throw IllegalStateException("No implementation of the server varp api is loaded.")
        }

        @Internal
        fun register(provider: VarpServerAPI) {
            this.provider = provider
        }

        @Internal
        fun unregister() {
            this.provider = null
        }
    }
}
