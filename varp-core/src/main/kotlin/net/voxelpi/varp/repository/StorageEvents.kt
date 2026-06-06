package net.voxelpi.varp.repository

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState

public object StorageEvents {

    @JvmRecord
    public data class ExternalWarpCreateEvent(
        val path: WarpPath,
        val state: WarpState,
    )

    @JvmRecord
    public data class ExternalFolderCreateEvent(
        val path: FolderPath,
        val state: FolderState,
    )
}
