package net.voxelpi.varp.repository.filetree

import net.kyori.adventure.key.Key
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer
import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.serializer.configurate.VarpConfigurateSerializers
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.NodeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import org.slf4j.LoggerFactory
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.reflect.KClass

object FileTreeStorage : Storage<FileTreeStorageConfig, StorageHandle> {

    override val id: Key = Key.key("varp", "file_tree")

    private val logger = LoggerFactory.getLogger(FileTreeStorage::class.java)

    override val configType: KClass<FileTreeStorageConfig>
        get() = FileTreeStorageConfig::class

    const val WARP_FILE_SUFFIX = ".warp"
    const val FOLDER_FILE_NAME = "folder"

    override suspend fun open(config: FileTreeStorageConfig): Result<StorageHandle> = runCatching {
        val handle = StorageHandle.Simple()

        config.dataDirectory().createDirectories()
        config.tempDirectory().createDirectories()

        // Create root state if it does not already exist.
        if (RootPath.file(config).notExists() || !RootPath.file(config).isRegularFile()) {
            RootPath.file(config).writeNodeState(config, FolderState.defaultRootState())
        }

        return@runCatching handle
    }

    @OptIn(ExperimentalPathApi::class)
    override suspend fun close(config: FileTreeStorageConfig, handle: StorageHandle): Result<Unit> = runCatching {
        if (config.tempDirectory().exists()) {
            config.tempDirectory().deleteRecursively()
        }
    }

    override suspend fun loadTree(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
    ): Result<TreeState> = runCatching {
        val state = MutableTreeState()

        // Create the root state, if it not already exists.
        config.dataDirectory().createDirectories()
        if (RootPath.file(config).notExists() || !RootPath.file(config).isRegularFile()) {
            RootPath.file(config).writeNodeState(config, FolderState.defaultRootState())
        }

        // Read the root state.
        state.root = RootPath.file(config).readFolderState(config)

        // Load all content warps & folders.
        val scanQueue = ArrayDeque<Pair<NodeParentPath, Path>>(listOf(Pair(RootPath, config.dataDirectory())))
        while (scanQueue.isNotEmpty()) {
            val (parentPath, parentDirectory) = scanQueue.removeFirst()
            for (entry in parentDirectory.listDirectoryEntries()) {
                when {
                    entry.isDirectory() -> {
                        // Construct the folder path.
                        val folderId = entry.name // The id of the folder is the name of the filesystem folder.
                        val folderPath = parentPath.folder(folderId)

                        // Read the folder state.
                        val folderConfig = folderPath.file(config)
                        if (folderConfig.notExists() || !folderConfig.isRegularFile()) {
                            throw IllegalStateException("Missing folder state file '$folderConfig'")
                        }
                        val folderState: FolderState = folderConfig.readFolderState(config)

                        // Register the folder in the tree state.
                        state.folders[folderPath] = folderState

                        // Queue the content of this directory to be scanned.
                        scanQueue.addLast(Pair(folderPath, entry))
                    }
                    entry.isRegularFile() -> {
                        // Ignore folder state file.
                        if (entry.name == "$FOLDER_FILE_NAME${config.format.extension}") {
                            continue
                        }

                        // Fail on unknown content.
                        if (!entry.name.endsWith("$WARP_FILE_SUFFIX${config.format.extension}")) {
                            throw IllegalStateException("Unsupported file type '${entry.normalize().absolutePathString()}'")
                        }

                        // Construct the warp path.
                        val warpId = entry.name.removeSuffix("$WARP_FILE_SUFFIX${config.format.extension}") // The id of the warp is the filename without the extension.
                        val warpPath = parentPath.warp(warpId)

                        // Read the warp state.
                        val warpState: WarpState = entry.readWarpState(config)

                        // Register the warp in the tree state.
                        state.warps[warpPath] = warpState
                    }
                    else -> {
                        throw IllegalStateException("Unsupported entry type '${entry.normalize().absolutePathString()}'")
                    }
                }
            }
        }

        return@runCatching state
    }

    @OptIn(ExperimentalPathApi::class)
    override suspend fun updateTree(config: FileTreeStorageConfig, handle: StorageHandle, state: TreeState): Result<Unit> = runCatching {
        val dataPath = config.dataDirectory()
        val backupPath = config.tempDirectory() / "backup_${System.nanoTime()}"
        var backupCreated = false

        try {
            // Move existing state to backup-location.
            if (dataPath.exists()) {
                Files.move(dataPath, backupPath)
                backupCreated = true
            }

            // Create the new state.
            dataPath.createDirectories()
            updateRoot(config, handle, state.root)

            for ((path, state) in state.folders) {
                createFolder(config, handle, path, state).getOrThrow()
            }
            for ((path, state) in state.warps) {
                createWarp(config, handle, path, state).getOrThrow()
            }
        } catch (exception: Exception) {
            // Delete incomplete new state.
            if (dataPath.isDirectory()) {
                dataPath.deleteRecursively()
            }

            // Rollback to the backup, if it exists.
            if (backupCreated) {
                runCatching { Files.move(backupPath, dataPath) }.onFailure {
                    logger.error("Failed to rollback to previous state '${backupPath.normalize().absolutePathString()}'", it)
                }
            }

            throw exception
        }

        // New state is valid, we can therefore delete the backup.
        if (backupCreated) {
            try {
                backupPath.deleteRecursively()
            } catch (exception: Exception) {
                logger.warn("Failed to delete backup state '${backupPath.normalize().absolutePathString()}'", exception)
            }
        }
    }

    override suspend fun createWarp(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: WarpPath,
        state: WarpState,
    ): Result<Unit> = runCatching {
        path.file(config).writeNodeState(config, state)
    }

    override suspend fun createFolder(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: FolderPath,
        state: FolderState,
    ): Result<Unit> = runCatching {
        path.file(config).writeNodeState(config, state)
    }

    override suspend fun updateWarp(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: WarpPath,
        state: WarpState,
    ): Result<Unit> = runCatching {
        path.file(config).writeNodeState(config, state)
    }

    override suspend fun updateFolder(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: FolderPath,
        state: FolderState,
    ): Result<Unit> = runCatching {
        path.file(config).writeNodeState(config, state)
    }

    override suspend fun updateRoot(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        state: FolderState,
    ): Result<Unit> = runCatching {
        RootPath.file(config).writeNodeState(config, state)
    }

    override suspend fun deleteWarp(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: WarpPath,
    ): Result<Unit> = runCatching {
        path.file(config).deleteIfExists()
    }

    @OptIn(ExperimentalPathApi::class)
    override suspend fun deleteFolder(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: FolderPath,
    ): Result<Unit> = runCatching {
        path.file(config).deleteIfExists() // Delete folder config.
        path.directory(config).deleteRecursively() // Delete folder recursive.
    }

    override suspend fun moveWarp(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        src: WarpPath,
        dst: WarpPath,
    ): Result<Unit> = runCatching {
        val srcPath = src.file(config)
        val dstPath = dst.file(config)
        Files.move(srcPath, dstPath)
    }

    override suspend fun moveFolder(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        src: FolderPath,
        dst: FolderPath,
    ): Result<Unit> = runCatching {
        val srcPath = src.directory(config)
        val dstPath = dst.directory(config)
        Files.move(srcPath, dstPath)
    }

    private fun createLoader(
        config: FileTreeStorageConfig,
        path: Path,
    ): AbstractConfigurationLoader<*> {
        return config.format.provider().apply {
            defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAll(
                        ConfigurateComponentSerializer.builder().apply {
                            // if (componentSerializer != null) {
                            //     scalarSerializer(componentSerializer)
                            //     outputStringComponents(true)
                            // }
                        }.build().serializers()
                    )
                    builder.registerAll(VarpConfigurateSerializers.serializers)
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            path(path)
        }.build()
    }

    private fun WarpPath.file(config: FileTreeStorageConfig): Path {
        return parent.directory(config) / "$id$WARP_FILE_SUFFIX${config.format.extension}"
    }

    private fun NodeParentPath.file(config: FileTreeStorageConfig): Path {
        return directory(config) / "$FOLDER_FILE_NAME${config.format.extension}"
    }

    private fun NodeParentPath.directory(config: FileTreeStorageConfig): Path {
        // Handle root path.
        if (this is RootPath) {
            return config.dataDirectory()
        }

        // Skip the first slash.
        val relativeFilesystemPath = this.toString().substring(1)
        return config.dataDirectory() / relativeFilesystemPath
    }

    private fun Path.writeNodeState(config: FileTreeStorageConfig, state: NodeState) {
        parent.createDirectories()
        val loader = createLoader(config, this)
        val node = loader.createNode()
        node.set(state)
        loader.save(node)
    }

    private fun Path.readWarpState(config: FileTreeStorageConfig): WarpState {
        val node = createLoader(config, this).load()
        return node.get() ?: throw Exception("invalid warp state in '${normalize().absolutePathString()}'")
    }

    private fun Path.readFolderState(config: FileTreeStorageConfig): FolderState {
        val node = createLoader(config, this).load()
        return node.get() ?: throw Exception("invalid folder state '${normalize().absolutePathString()}'")
    }
}
