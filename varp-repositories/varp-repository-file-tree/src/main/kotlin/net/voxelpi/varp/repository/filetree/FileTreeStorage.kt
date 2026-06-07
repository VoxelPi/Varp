package net.voxelpi.varp.repository.filetree

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
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.reflect.KClass

object FileTreeStorage : Storage<FileTreeStorageConfig, StorageHandle> {

    private val logger = LoggerFactory.getLogger(FileTreeStorage::class.java)

    override val configType: KClass<FileTreeStorageConfig>
        get() = FileTreeStorageConfig::class

    const val WARP_FILE_SUFFIX = ".warp"
    const val FOLDER_FILE_NAME = "folder"

    override suspend fun open(config: FileTreeStorageConfig): Result<StorageHandle> = runCatching {
        val handle = StorageHandle.Simple()

        config.dataDirectory().createDirectories()
        config.tempDirectory().createDirectories()

        // Create root file if it does not already exist.
        if (RootPath.file(config).notExists()) {
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

    override suspend fun loadTree(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
    ): Result<TreeState> = runCatching {
        val state = MutableTreeState()

        // Create the data directory if it does not already exist.
        if (!config.dataDirectory().isDirectory()) {
            config.dataDirectory().createDirectories()
        }

        // Create root file if it does not already exist.
        if (RootPath.file(config).notExists()) {
            RootPath.file(config).writeNodeState(config, FolderState.defaultRootState())
        }

        // Load content.
        val rootState = loadRoot(config, config.path).getOrThrow()
        val (warps, folders) = loadContainerContent(config, RootPath, config.path).getOrThrow()
        state.root = rootState
        state.warps += warps
        state.folders += folders

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
                updateFolder(config, handle, path, state).getOrThrow()
            }
            for ((path, state) in state.warps) {
                updateWarp(config, handle, path, state).getOrThrow()
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

    private fun WarpPath.file(config: FileTreeStorageConfig): Path {
        return parent.directory(config).resolve("$id$WARP_FILE_SUFFIX${config.format.extension}")
    }

    private fun NodeParentPath.file(config: FileTreeStorageConfig): Path {
        return directory(config).resolve("$FOLDER_FILE_NAME${config.format.extension}")
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
        createDirectories()
        val loader = createLoader(config, this)
        val node = loader.createNode()
        node.set(state)
        loader.save(node)
    }

    private fun loadContainerContent(
        config: FileTreeStorageConfig,
        parent: NodeParentPath,
        path: Path,
    ): Result<Pair<Map<WarpPath, WarpState>, Map<FolderPath, FolderState>>> {
        val warps = mutableMapOf<WarpPath, WarpState>()
        val folders = mutableMapOf<FolderPath, FolderState>()

        for (file in path.listDirectoryEntries("*$WARP_FILE_SUFFIX${config.format.extension}")) {
            // Load child warp state.
            val (warpPath, warpState) = loadWarp(config, parent, file).getOrElse {
                return Result.failure(Exception("Unable to load warp state data \"${file.toAbsolutePath()}\": ${it.message}"))
            }
            warps[warpPath] = warpState
        }

        for (folder in Files.newDirectoryStream(path) { file -> Files.isDirectory(file) }) {
            // Load child folder state.
            val (folderPath, folderState) = loadFolder(config, parent, folder).getOrElse {
                return Result.failure(Exception("Unable to load folder state data \"${folder.toAbsolutePath()}\": ${it.message}"))
            }
            folders[folderPath] = folderState

            // Load child folder children.
            val (childWarps, childFolders) = loadContainerContent(config, folderPath, folder).getOrElse {
                return Result.failure(it)
            }
            warps.putAll(childWarps)
            folders.putAll(childFolders)
        }

        return Result.success(Pair(warps, folders))
    }

    private fun loadWarp(
        config: FileTreeStorageConfig,
        parent: NodeParentPath,
        path: Path,
    ): Result<Pair<WarpPath, WarpState>> {
        return runCatching {
            check(Files.exists(path)) { "Warp configuration missing ($path)" }

            val node = createLoader(config, path).load()

            val name = path.name.removeSuffix("$WARP_FILE_SUFFIX${config.format.extension}")
            val state: WarpState = node.get() ?: throw Exception("invalid warp state ($path)")

            Pair(parent.warp(name), state)
        }
    }

    private fun loadFolder(
        config: FileTreeStorageConfig,
        parent: NodeParentPath,
        path: Path,
    ): Result<Pair<FolderPath, FolderState>> {
        return runCatching {
            check(Files.isDirectory(path)) { "Path doesn't lead to a folder ($path)" }

            val folderConfig = path.resolve(FOLDER_FILE_NAME + config.format.extension)
            check(Files.exists(path)) { "Folder configuration missing ($path)" }

            val node = createLoader(config, folderConfig).load()

            val name = path.name
            val state: FolderState = node.get() ?: throw Exception("invalid folder state ($path)")

            Pair(parent.folder(name), state)
        }
    }

    private fun loadRoot(
        config: FileTreeStorageConfig,
        path: Path,
    ): Result<FolderState> {
        return runCatching {
            check(Files.isDirectory(path)) { "Path doesn't lead to a folder ($path)" }

            val moduleConfig = path.resolve(FOLDER_FILE_NAME + config.format.extension)
            check(Files.exists(path)) { "Root configuration missing ($path)" }

            val node = createLoader(config, moduleConfig).load()
            val state: FolderState = node.get() ?: throw Exception("invalid module state ($path)")

            state
        }
    }
}
