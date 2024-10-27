package net.voxelpi.varp.repository.filetree

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.voxelpi.varp.serializer.configurate.VarpConfigurateSerializers
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.RepositoryLoader
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.repository.SimpleRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.WarpState
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

@RepositoryType("file-tree")
class FileTreeRepository(
    id: String,
    val path: Path,
    val format: RepositoryFileFormat,
    val componentSerializer: ComponentSerializer<Component, *, String>? = null,
) : SimpleRepository(id) {

    override val registryView: TreeStateRegistry = TreeStateRegistry()

    @RepositoryLoader
    public constructor(id: String, path: Path, config: FileTreeRepositoryConfig) : this(
        id,
        path,
        RepositoryFileFormat.format(config.format)!!,
        if (!config.componentsAsObjects) MiniMessage.miniMessage() else null,
    )

    override suspend fun activate(): Result<Unit> {
        return super.activate()
    }

    override suspend fun deactivate(): Result<Unit> {
        return super.deactivate()
    }

    // region content functions

    override suspend fun handleLoad(): Result<Unit> {
        return runCatching {
            // Create main directory if it does not already exist.
            if (!path.isDirectory()) {
                path.createDirectories()
            }

            // Create root file if it does not already exist.
            if (RootPath.file().notExists()) {
                save(FolderState.defaultRootState())
            }

            // Load content.
            val rootState = loadRoot(path).getOrThrow()
            val (warps, folders) = loadContainerContent(RootPath, path).getOrThrow()
            registryView.root = rootState
            registryView.warps += warps
            registryView.folders += folders
        }
    }

    override suspend fun handleCreate(path: WarpPath, state: WarpState): Result<Unit> {
        return handleSave(path, state)
    }

    override suspend fun handleCreate(path: FolderPath, state: FolderState): Result<Unit> {
        return handleSave(path, state)
    }

    override suspend fun handleSave(path: WarpPath, state: WarpState): Result<Unit> {
        return runCatching {
            val loader = loader().apply {
                path(path.file())
            }.build()

            val node = loader.createNode()
            node.set(state)
            loader.save(node)
        }
    }

    override suspend fun handleSave(path: FolderPath, state: FolderState): Result<Unit> {
        return runCatching {
            val directory = path.directory()
            if (!directory.isDirectory()) {
                directory.createDirectories()
            }

            val loader = loader().apply {
                path(path.file())
            }.build()

            val node = loader.createNode()
            node.set(state)
            loader.save(node)
        }
    }

    override suspend fun handleSave(state: FolderState): Result<Unit> {
        return runCatching {
            val directory = RootPath.directory()
            if (!directory.isDirectory()) {
                directory.createDirectories()
            }

            val loader = loader().apply {
                path(RootPath.file())
            }.build()

            val node = loader.createNode()
            node.set(state)
            loader.save(node)
        }
    }

    override suspend fun handleDelete(path: WarpPath): Result<Unit> {
        return runCatching {
            path.file().deleteIfExists()
        }
    }

    @OptIn(ExperimentalPathApi::class)
    override suspend fun handleDelete(path: FolderPath): Result<Unit> {
        return runCatching {
            path.file().deleteIfExists() // Delete folder config.
            path.directory().deleteRecursively() // Delete folder recursive.
        }
    }

    override suspend fun handleMove(src: WarpPath, dst: WarpPath): Result<Unit> {
        return runCatching {
            val srcPath = src.file()
            val dstPath = dst.file()
            Files.move(srcPath, dstPath)
        }
    }

    override suspend fun handleMove(src: FolderPath, dst: FolderPath): Result<Unit> {
        return runCatching {
            val srcPath = src.directory()
            val dstPath = dst.directory()
            Files.move(srcPath, dstPath)
        }
    }

    // endregion

    private fun loader(): AbstractConfigurationLoader.Builder<*, *> {
        return format.provider().apply {
            defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAll(
                        ConfigurateComponentSerializer.builder().apply {
                            if (componentSerializer != null) {
                                scalarSerializer(componentSerializer)
                                outputStringComponents(true)
                            }
                        }.build().serializers()
                    )
                    builder.registerAll(VarpConfigurateSerializers.serializers)
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
        }
    }

    private fun WarpPath.file(): Path {
        return parent.directory().resolve("$id$WARP_FILE_SUFFIX${format.extension}")
    }

    private fun NodeParentPath.file(): Path {
        return directory().resolve("$FOLDER_FILE_NAME${format.extension}")
    }

    private fun NodeParentPath.directory(): Path {
        // Handle root path.
        if (this is RootPath) {
            return path
        }

        // Skip the first slash.
        return path.resolve(this.toString().substring(1))
    }

    private fun loadContainerContent(
        parent: NodeParentPath,
        path: Path,
    ): Result<Pair<Map<WarpPath, WarpState>, Map<FolderPath, FolderState>>> {
        val warps = mutableMapOf<WarpPath, WarpState>()
        val folders = mutableMapOf<FolderPath, FolderState>()

        for (file in path.listDirectoryEntries("*$WARP_FILE_SUFFIX${format.extension}")) {
            // Load child warp state.
            val (warpPath, warpState) = loadWarp(parent, file).getOrElse {
                return Result.failure(Exception("Unable to load warp state data \"${file.toAbsolutePath()}\": ${it.message}"))
            }
            warps[warpPath] = warpState
        }

        for (folder in Files.newDirectoryStream(path) { file -> Files.isDirectory(file) }) {
            // Load child folder state.
            val (folderPath, folderState) = loadFolder(parent, folder).getOrElse {
                return Result.failure(Exception("Unable to load folder state data \"${folder.toAbsolutePath()}\": ${it.message}"))
            }
            folders[folderPath] = folderState

            // Load child folder children.
            val (childWarps, childFolders) = loadContainerContent(folderPath, folder).getOrElse {
                return Result.failure(it)
            }
            warps.putAll(childWarps)
            folders.putAll(childFolders)
        }

        return Result.success(Pair(warps, folders))
    }

    private fun loadWarp(parent: NodeParentPath, path: Path): Result<Pair<WarpPath, WarpState>> {
        return runCatching {
            check(Files.exists(path)) { "Warp configuration missing ($path)" }

            val node = loader().path(path).build().load()

            val name = path.name.removeSuffix("$WARP_FILE_SUFFIX${format.extension}")
            val state: WarpState = node.get() ?: throw Exception("invalid warp state ($path)")

            Pair(parent.warp(name), state)
        }
    }

    private fun loadFolder(parent: NodeParentPath, path: Path): Result<Pair<FolderPath, FolderState>> {
        return runCatching {
            check(Files.isDirectory(path)) { "Path doesn't lead to a folder ($path)" }

            val folderConfig = path.resolve(FOLDER_FILE_NAME + format.extension)
            check(Files.exists(path)) { "Folder configuration missing ($path)" }

            val node = loader().path(folderConfig).build().load()

            val name = path.name
            val state: FolderState = node.get() ?: throw Exception("invalid folder state ($path)")

            Pair(parent.folder(name), state)
        }
    }

    private fun loadRoot(path: Path): Result<FolderState> {
        return runCatching {
            check(Files.isDirectory(path)) { "Path doesn't lead to a folder ($path)" }

            val moduleConfig = path.resolve(FOLDER_FILE_NAME + format.extension)
            check(Files.exists(path)) { "Root configuration missing ($path)" }

            val node = loader().path(moduleConfig).build().load()
            val state: FolderState = node.get() ?: throw Exception("invalid module state ($path)")

            state
        }
    }

    companion object {
        const val WARP_FILE_SUFFIX = ".warp"

        const val FOLDER_FILE_NAME = "folder"
    }
}
