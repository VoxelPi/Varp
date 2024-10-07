package net.voxelpi.varp.api

/**
 * Action to be performed when a node already exists at the destination path when copying or moving a node.
 */
enum class DuplicatesStrategy {

    /**
     * Replace the existing node.
     */
    REPLACE_EXISTING,

    /**
     * Keep the existing node.
     */
    SKIP,

    /**
     * Throw an exception.
     */
    FAIL,
}
