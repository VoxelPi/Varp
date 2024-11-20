package net.voxelpi.varp.mod.server.api

/**
 * Stores information about the platform the varp server mod runs on.
 */
public interface ServerPlatform {

    /**
     * If the server is a dedicated server.
     */
    public val isDedicated: Boolean

    /**
     * The name of the platform.
     */
    public val name: String

    /**
     * The brand of the platform implementation.
     */
    public val brand: String

    /**
     * The version of the platform.
     */
    public val version: String
}
