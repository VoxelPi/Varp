package net.voxelpi.varp.repository.filetree

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.serializer.configurate.VarpConfigurateSerializers
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.TreeRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.WarpState
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

class FileTreeTreeRepository(
    override val id: String,
    val path: Path,
    val format: RepositoryFileFormat,
    val componentSerializer: ComponentSerializer<Component, *, String>? = null,
) : TreeRepository {

    override val registry: TreeStateRegistry = TreeStateRegistry()

    init {
        load().getOrThrow()
    }

    override fun reload(): Result<Unit> {
        registry.clear()
        return load()
    }

    private fun load(): Result<Unit> {
        return runCatching {
            // Create main directory if it does not already exist.
            if (!path.isDirectory()) {
                path.createDirectories()
            }

            // Create root file if it does not already exist.
            if (RootPath.file().notExists()) {
                saveRootState(FolderState.defaultRootState())
            }

            // Load content.
            val rootState = loadRoot(path).getOrThrow()
            val (warps, folders) = loadContainerContent(RootPath, path).getOrThrow()
            registry.root = rootState
            registry.warps += warps
            registry.folders += folders
        }
    }

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        return saveWarpState(path, state)
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        return saveFolderState(path, state)
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        return runCatching {
            val loader = loader().apply {
                path(path.file())
            }.build()

            val node = loader.createNode()
            node.set(state)
            loader.save(node)

            // Update the registry.
            registry[path] = state
        }
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
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

            // Update the registry.
            registry[path] = state
        }
    }

    override fun saveRootState(state: FolderState): Result<Unit> {
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

            // Update the registry.
            registry.root = state
        }
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        return runCatching {
            path.file().deleteIfExists()

            // Update the registry.
            registry.remove(path)
        }
    }

    override fun deleteFolderState(path: FolderPath): Result<Unit> {
        return runCatching {
            path.file().deleteIfExists()
            path.directory().deleteIfExists()

            // Update the registry.
            registry.remove(path)
        }
    }

    override fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit> {
        return runCatching {
            val srcPath = src.file()
            val dstPath = dst.file()
            Files.move(srcPath, dstPath)

            registry.move(src, dst) ?: return Result.failure(WarpNotFoundException(src))
            Unit
        }
    }

    override fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit> {
        return runCatching {
            val srcPath = src.directory()
            val dstPath = dst.directory()
            Files.move(srcPath, dstPath)

            registry.move(src, dst) ?: return Result.failure(FolderNotFoundException(src))
            Unit
        }
    }

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
