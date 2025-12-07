package net.voxelpi.varp.mod.client.api

import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.client.api.warp.ClientRepository
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.Warp
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the client side varp implementation.
 */
public interface VarpClient {

    /**
     * The version of the client varp mod.
     */
    public val version: String

    /**
     * Information about the varp client mod.
     */
    public val info: VarpClientInformation

    /**
     * Information about the varp server mod on the server the client is currently connected to,
     * or null if the client is currently not connected to any server.
     */
    public val serverInfo: VarpServerInformation?

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
    public fun openExplorer(path: NodeParentPath = RootPath)

    /**
     * Returns if the client-server varp bridge is currently active.
     */
    public fun isBridgeEnabled(): Boolean {
        return serverInfo != null
    }

    /**
     * Teleport to the given [warp].
     */
    public fun teleportToWarp(warp: Warp)

    public companion object {
        private var provider: VarpClient? = null

        /**
         * Returns the currently loaded api implementation.
         */
        public fun get(): VarpClient {
            return provider ?: throw IllegalStateException("No implementation of the client varp api is loaded.")
        }

        @Internal
        public fun register(provider: VarpClient) {
            this.provider = provider
        }

        @Internal
        public fun unregister() {
            this.provider = null
        }
    }
}
