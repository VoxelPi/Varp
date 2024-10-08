package net.voxelpi.varp.mod.client.api

import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the client side varp implementation.
 */
interface VarpClientAPI {

    val version: String

    companion object {
        private var provider: VarpClientAPI? = null

        /**
         * Returns the currently loaded api implementation.
         */
        fun get(): VarpClientAPI {
            return provider ?: throw IllegalStateException("No implementation of the client varp api is loaded.")
        }

        @Internal
        fun register(provider: VarpClientAPI) {
            this.provider = provider
        }

        @Internal
        fun unregister() {
            this.provider = null
        }
    }
}
