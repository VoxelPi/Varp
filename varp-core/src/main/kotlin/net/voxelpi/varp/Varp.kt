package net.voxelpi.varp

/**
 * Provides varp api.
 */
public object Varp {

    public val version: String
        get() = VarpBuildParameters.VERSION

    public val exactVersion: String
        get() = "${VarpBuildParameters.VERSION}-${VarpBuildParameters.GIT_COMMIT}"
}
