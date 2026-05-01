package net.voxelpi.varp.extras.cloud

import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.repository.compositor.Compositor
import net.voxelpi.varp.tree.Tree
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.kotlin.extension.cloudKey

public object VarpCommandArguments {

    public val TREE: CloudKey<Tree> = cloudKey("varp:tree")

    public val COMPOSITOR: CloudKey<Compositor> = cloudKey("varp:compositor")

    public val ENVIRONMENT: CloudKey<VarpEnvironment> = cloudKey("varp:environment")
}
