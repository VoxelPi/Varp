package net.voxelpi.varp.option

import net.voxelpi.varp.DuplicatesStrategy

/**
 * Option that controls what should happen with duplicates when copying / moving nodes.
 */
public val DuplicatesStrategyOption: Option<DuplicatesStrategy> = Option(DuplicatesStrategy.FAIL)

/**
 * Option that controls if the mounts of a compositor should move.
 */
public val MoveMountsOptions: Option<Boolean> = Option(true)
