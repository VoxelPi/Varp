package net.voxelpi.varp.repository

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.treeState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepositoryTest {

    @Test
    fun `test repository create`(): Unit = runBlocking {
        val repository = Repository("repo", EphemeralStorage, Unit)
        repository.open().getOrThrow()
        val state = treeState {
            folder("test_folder_0") {
                folder("test_folder_1") {
                    folder("test_folder_2") {}
                    warp("test_warp_2", Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f, "Test Warp 2")
                }
                warp("test_warp_1", Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f)
            }
            warp("test_warp_0", Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f)
        }
        repository.update(state)

        assertTrue(FolderPath("/test_folder_0/") in repository)
        assertTrue(WarpPath("/test_warp_0") in repository)
        assertTrue(FolderPath("/test_folder_0/test_folder_1/") in repository)
        assertTrue(WarpPath("/test_folder_0/test_warp_1") in repository)
        assertTrue(FolderPath("/test_folder_0/test_folder_1/test_folder_2/") in repository)
        assertTrue(WarpPath("/test_folder_0/test_folder_1/test_warp_2") in repository)
        assertEquals(state, repository.state)
    }
}
