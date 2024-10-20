package net.voxelpi.varp.mod.client.api

import net.voxelpi.varp.mod.client.api.warp.ClientRepository
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the client side varp implementation.
 */
public interface VarpClientAPI {

    /**
     * The version of the client varp mod.
     */
    public val version: String

    /**
     * The varp client repository.
     */
    public val repository: ClientRepository

    /**
     * The varp tree.
     */
    public val tree: Tree

    /**
     * Opens the varp explorer gui, displaying the content of the container specified by the given [path].
     */
    public fun openExplorer(path: NodeParentPath)

    public companion object {
        private var provider: VarpClientAPI? = null

        /**
         * Returns the currently loaded api implementation.
         */
        public fun get(): VarpClientAPI {
            return provider ?: throw IllegalStateException("No implementation of the client varp api is loaded.")
        }

        @Internal
        public fun register(provider: VarpClientAPI) {
            this.provider = provider
        }

        @Internal
        public fun unregister() {
            this.provider = null
        }
    }
}
