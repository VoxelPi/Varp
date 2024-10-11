package net.voxelpi.varp

import net.voxelpi.varp.api.VarpAPI

object VarpImpl : VarpAPI {

    override val version: String
        get() = VarpBuildParameters.VERSION

    override val exactVersion: String
        get() = "${VarpBuildParameters.VERSION}-${VarpBuildParameters.GIT_COMMIT}"

    fun load() {
        VarpAPI.register(this)
    }
}
