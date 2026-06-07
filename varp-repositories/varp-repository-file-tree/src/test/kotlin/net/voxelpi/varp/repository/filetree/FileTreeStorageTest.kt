package net.voxelpi.varp.repository.filetree

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.repository.StorageInstance
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState
import net.voxelpi.varp.tree.state.treeState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class FileTreeStorageTest {

    @TempDir
    lateinit var testDirectory: Path

    lateinit var storage: StorageInstance<FileTreeStorageConfig, StorageHandle>

    @BeforeEach
    fun setup() {
        storage = FileTreeStorage.createInstance(FileTreeStorageConfig(testDirectory, FileTreeStorageFormat.JSON))
    }

    @Test
    fun `test open and close file tree storage`() {
        runBlocking { storage.open().getOrThrow() }
        runBlocking { storage.close().getOrThrow() }
    }

    @Test
    fun `test loading an empty file tree storage`() {
        runBlocking { storage.open().getOrThrow() }
        runBlocking {
            val state = storage.loadTree().getOrThrow()
            assertEquals(
                treeState(FolderState.defaultRootState()) {},
                state,
            )
        }
        runBlocking { storage.close().getOrThrow() }
    }

    @Test
    fun `test storing and loading in file tree storage`() {
        val expectedState = treeState(name = "MyRoot") {
            folder("my_folder", name = "MyFolder") {
                folder("my_folder", name = "MyFolder") {
                    warp("my_warp_1", Key.key("minecraft:overworld"), 5.0, -4.0, 3.0, -2.0f, 1.0f, name = "MyWarp_1")
                    warp("my_warp_2", Key.key("minecraft:overworld"), 5.0, -4.0, 3.0, -2.0f, 1.0f, name = "MyWarp_2")
                }
                warp("my_warp", Key.key("minecraft:overworld"), 5.0, -4.0, 3.0, -2.0f, 1.0f, name = "MyWarp")
            }
            warp("my_warp", Key.key("minecraft:overworld"), 5.0, -4.0, 3.0, -2.0f, 1.0f, name = "MyWarp")
        }

        runBlocking { storage.open().getOrThrow() }
        runBlocking { storage.updateTree(expectedState) }
        val actualState = runBlocking { storage.loadTree().getOrThrow() }
        runBlocking { storage.close().getOrThrow() }

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `test update root in file tree storage`() {
        val expectedState = FolderState(
            name = "My custom folder",
            description = listOf("A folder created by this test.", "Should exist!"),
            tags = setOf("a_tag", "another_tag"),
            properties = mapOf("first_property" to "thing", "second_property" to "another thing"),
        )

        runBlocking { storage.open().getOrThrow() }
        runBlocking { storage.updateRoot(expectedState) }
        val actualState = runBlocking { storage.loadTree().getOrThrow() }
        runBlocking { storage.close().getOrThrow() }

        assertEquals(expectedState, actualState.root)
    }

    @Test
    fun `test folder in file tree storage`() {
        val path1 = RootPath.folder("test")
        val path2 = RootPath.folder("other_test")
        val expectedState = FolderState(
            name = "My custom folder",
            description = listOf("A folder created by this test.", "Should exist!"),
            tags = setOf("a_tag", "another_tag"),
            properties = mapOf("first_property" to "thing", "second_property" to "another thing"),
        )

        runBlocking { storage.open().getOrThrow() }

        runBlocking { storage.createFolder(path1, FolderState.defaultRootState()).getOrThrow() }
        val actualState1 = runBlocking { storage.loadTree().getOrThrow() }
        assertEquals(FolderState.defaultRootState(), actualState1[path1])

        runBlocking { storage.updateFolder(path1, expectedState).getOrThrow() }
        val actualState2 = runBlocking { storage.loadTree().getOrThrow() }
        assertEquals(expectedState, actualState2[path1])

        runBlocking { storage.moveFolder(path1, path2).getOrThrow() }
        val actualState3 = runBlocking { storage.loadTree().getOrThrow() }
        assertFalse(path1 in actualState3)
        assertEquals(expectedState, actualState3[path2])

        runBlocking { storage.deleteFolder(path2).getOrThrow() }
        val actualState4 = runBlocking { storage.loadTree().getOrThrow() }
        assertFalse(path2 in actualState4)

        runBlocking { storage.close().getOrThrow() }
    }

    @Test
    fun `test warp in file tree storage`() {
        val path1 = RootPath.warp("test")
        val path2 = RootPath.warp("other_test")
        val state1 = WarpState(
            name = "My warp",
            location = MinecraftLocation(
                Key.key("minecraft:overworld"),
                3.2,
                1.1,
                -3.0,
                25.2f,
                -32f
            ),
            description = listOf("A warp created by this test."),
            tags = setOf("a_tag"),
            properties = mapOf("first_property" to "thing"),
        )
        val state2 = WarpState(
            name = "My test warp",
            location = MinecraftLocation(
                Key.key("minecraft:overworld"),
                6.2,
                -3.1,
                3.0,
                -2.2f,
                2f
            ),
            description = listOf("A warp created by this test.", "Should exist!"),
            tags = setOf("a_tag", "another_tag"),
            properties = mapOf("first_property" to "thing", "second_property" to "another thing"),
        )

        runBlocking { storage.open().getOrThrow() }

        runBlocking { storage.createWarp(path1, state1).getOrThrow() }
        val actualState1 = runBlocking { storage.loadTree().getOrThrow() }
        assertEquals(state1, actualState1[path1])

        runBlocking { storage.updateWarp(path1, state2).getOrThrow() }
        val actualState2 = runBlocking { storage.loadTree().getOrThrow() }
        assertEquals(state2, actualState2[path1])

        runBlocking { storage.moveWarp(path1, path2).getOrThrow() }
        val actualState3 = runBlocking { storage.loadTree().getOrThrow() }
        assertFalse(path1 in actualState3)
        assertEquals(state2, actualState3[path2])

        runBlocking { storage.deleteWarp(path2).getOrThrow() }
        val actualState4 = runBlocking { storage.loadTree().getOrThrow() }
        assertFalse(path2 in actualState4)

        runBlocking { storage.close().getOrThrow() }
    }
}
