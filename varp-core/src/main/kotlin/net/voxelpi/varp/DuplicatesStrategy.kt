package net.voxelpi.varp

/**
 * Action to be performed when a node already exists at the destination path when copying or moving a node.
 */
public enum class DuplicatesStrategy {

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
