package net.voxelpi.varp.mod.server.api

/**
 * Stores information about the platform the varp server mod runs on.
 */
public interface ServerPlatform {

    /**
     * If the server is a dedicated server.
     */
    public val isDedicated: Boolean
}
