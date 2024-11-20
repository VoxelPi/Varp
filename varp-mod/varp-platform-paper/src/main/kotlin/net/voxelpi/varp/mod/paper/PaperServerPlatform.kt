package net.voxelpi.varp.mod.paper

import io.papermc.paper.ServerBuildInfo
import net.voxelpi.varp.mod.server.ServerPlatformImpl

class PaperServerPlatform : ServerPlatformImpl {

    override val isDedicated: Boolean = true

    override val name: String = "paper"

    override val brand: String

    override val version: String

    init {
        val buildInfo = ServerBuildInfo.buildInfo()

        brand = buildInfo.brandName()
        version = if (buildInfo.buildNumber().isPresent) {
            "MC ${buildInfo.minecraftVersionName()} (build ${buildInfo.buildNumber().orElseGet { 0 }})"
        } else if (buildInfo.gitCommit().isPresent) {
            "MC ${buildInfo.minecraftVersionName()} (commit ${buildInfo.gitCommit().orElseGet { "unknown" }})"
        } else {
            "MC ${buildInfo.minecraftVersionName()} (unknown)"
        }
    }
}
