package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.path.NodePath

/**
 * An Exception that is thrown when trying to access state of a compositor that has no mounted repository.
 */
public class MissingMountException(public val path: NodePath) : Exception("No mount present for the given path: \"$path\".")
