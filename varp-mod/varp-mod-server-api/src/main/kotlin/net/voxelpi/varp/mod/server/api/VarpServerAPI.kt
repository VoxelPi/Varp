package net.voxelpi.varp.mod.server.api

import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the server side varp implementation.
 */
interface VarpServerAPI {

    val version: String

    companion object {
        private var provider: VarpServerAPI? = null

        /**
         * Returns the currently loaded api implementation.
         */
        fun get(): VarpServerAPI {
            return provider ?: throw IllegalStateException("No implementation fo the client varp api is loaded.")
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
