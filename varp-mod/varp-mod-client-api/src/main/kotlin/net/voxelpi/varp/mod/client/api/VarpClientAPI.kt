package net.voxelpi.varp.mod.client.api

import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the client side varp implementation.
 */
public interface VarpClientAPI {

    public val version: String

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
