package net.voxelpi.varp.repository

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState

public object StorageEvents {

    @JvmRecord
    public data class TreeStateChangeEvent(
        val newState: TreeState,
    )

    @JvmRecord
    public data class WarpCreateEvent(
        val path: WarpPath,
        val state: WarpState,
    )

    @JvmRecord
    public data class FolderCreateEvent(
        val path: FolderPath,
        val state: FolderState,
    )

    @JvmRecord
    public data class WarpStateChangeEvent(
        val path: WarpPath,
        val newState: WarpState,
    )

    @JvmRecord
    public data class FolderStateChangeEvent(
        val path: FolderPath,
        val newState: FolderState,
    )

    @JvmRecord
    public data class RootStateChangeEvent(
        val newState: FolderState,
    )

    @JvmRecord
    public data class WarpDeleteEvent(
        val path: WarpPath,
    )

    @JvmRecord
    public data class FolderDeleteEvent(
        val path: FolderPath,
    )

    @JvmRecord
    public data class WarpPathChangeEvent(
        val oldPath: WarpPath,
        val newPath: WarpPath,
    )

    @JvmRecord
    public data class FolderPathChangeEvent(
        val oldPath: FolderPath,
        val newPath: FolderPath,
    )
}
