package net.voxelpi.varp.repository.filetree

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer
import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.repository.StorageCapability
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.serializer.configurate.VarpConfigurateSerializers
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.EnumSet
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.reflect.KClass

object FileTreeStorage : Storage<FileTreeStorageConfig, StorageHandle> {

    override val capabilities: EnumSet<StorageCapability> = EnumSet.of(
        StorageCapability.RECURSIVE_DELETE,
        StorageCapability.RECURSIVE_MOVE,
    )

    override val configType: KClass<*>
        get() = FileTreeStorageConfig::class

    const val WARP_FILE_SUFFIX = ".warp"
    const val FOLDER_FILE_NAME = "folder"

    override suspend fun open(config: FileTreeStorageConfig): Result<StorageHandle> = runCatching { StorageHandle.Simple() }

    override suspend fun close(config: FileTreeStorageConfig, handle: StorageHandle): Result<Unit> = runCatching {}

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

    override suspend fun loadContent(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
    ): Result<TreeState> = runCatching {
        val state = MutableTreeState()

        // Create main directory if it does not already exist.
        if (!config.path.isDirectory()) {
            config.path.createDirectories()
        }

        // Create root file if it does not already exist.
        if (RootPath.file(config).notExists()) {
            saveRoot(config, handle, FolderState.defaultRootState())
        }

        // Load content.
        val rootState = loadRoot(config, config.path).getOrThrow()
        val (warps, folders) = loadContainerContent(config, RootPath, config.path).getOrThrow()
        state.root = rootState
        state.warps += warps
        state.folders += folders

        return@runCatching state
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

    override suspend fun createWarp(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: WarpPath,
        state: WarpState,
    ): Result<Unit> = saveWarp(config, handle, path, state)

    override suspend fun createFolder(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: FolderPath,
        state: FolderState,
    ): Result<Unit> = saveFolder(config, handle, path, state)

    override suspend fun saveWarp(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: WarpPath,
        state: WarpState,
    ): Result<Unit> = runCatching {
        val loader = createLoader(config, path.file(config))
        val node = loader.createNode()
        node.set(state)
        loader.save(node)
    }

    override suspend fun saveFolder(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        path: FolderPath,
        state: FolderState,
    ): Result<Unit> = runCatching {
        val directory = path.directory(config)
        if (!directory.isDirectory()) {
            directory.createDirectories()
        }

        val loader = createLoader(config, path.file(config))
        val node = loader.createNode()
        node.set(state)
        loader.save(node)
    }

    override suspend fun saveRoot(
        config: FileTreeStorageConfig,
        handle: StorageHandle,
        state: FolderState,
    ): Result<Unit> = runCatching {
        val directory = RootPath.directory(config)
        if (!directory.isDirectory()) {
            directory.createDirectories()
        }

        val loader = createLoader(config, RootPath.file(config))
        val node = loader.createNode()
        node.set(state)
        loader.save(node)
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
            return config.path
        }

        // Skip the first slash.
        return config.path.resolve(this.toString().substring(1))
    }
}
