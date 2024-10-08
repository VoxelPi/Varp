package net.voxelpi.varp.api

import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Provides the api for the varp implementation.
 */
interface VarpAPI {

    val version: String

    val exactVersion: String

    companion object {
        private var provider: VarpAPI? = null

        /**
         * Returns the currently loaded api implementation.
         */
        fun get(): VarpAPI {
            return provider ?: throw IllegalStateException("No implementation of the varp api is loaded.")
        }

        @Internal
        fun register(provider: VarpAPI) {
            this.provider = provider
        }

        @Internal
        fun unregister() {
            this.provider = null
        }
    }
}
