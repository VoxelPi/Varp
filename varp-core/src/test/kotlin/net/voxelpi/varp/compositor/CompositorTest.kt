package net.voxelpi.varp.compositor

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.repository.EphemeralStorage
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class CompositorTest {

    @Test
    fun `test compositor create`(): Unit = runBlocking {
        val repo0 = Repository("repo_0", EphemeralStorage, Unit)
        val repo1 = Repository("repo_1", EphemeralStorage, Unit)
        val repo2 = Repository("repo_2", EphemeralStorage, Unit)
        val repo3 = Repository("repo_3", EphemeralStorage, Unit)

        repo0.open().getOrThrow()
        repo1.open().getOrThrow()
        repo2.open().getOrThrow()
        repo3.open().getOrThrow()

        val compositor = Compositor(
            listOf(
                CompositorMount(NodeParentPath.parse("/").getOrThrow(), repo0, RootPath) {},
                CompositorMount(NodeParentPath.parse("/data1/").getOrThrow(), repo1, RootPath) {},
                CompositorMount(NodeParentPath.parse("/data2/").getOrThrow(), repo2, RootPath) {},
                CompositorMount(NodeParentPath.parse("/data2/data3/").getOrThrow(), repo3, RootPath) {},
            ),
        )

        compositor.create(
            FolderPath("/test_folder_0/"),
            FolderState(ComponentTemplate("test folder 0")),
        ).getOrThrow()
        assertTrue(FolderPath("/test_folder_0/") in compositor)
        assertTrue(FolderPath("/test_folder_0/") in repo0)

        compositor.create(
            WarpPath("/test_warp_0"),
            WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), ComponentTemplate("test warp 0")),
        ).getOrThrow()
        assertTrue(WarpPath("/test_warp_0") in compositor)
        assertTrue(WarpPath("/test_warp_0") in repo0)

        compositor.create(
            FolderPath("/data1/test_folder_1/"),
            FolderState(ComponentTemplate("test folder 1")),
        ).getOrThrow()
        assertTrue(FolderPath("/data1/test_folder_1/") in compositor)
        assertTrue(FolderPath("/test_folder_1/") in repo1)

        compositor.create(
            WarpPath("/data1/test_warp_1"),
            WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), ComponentTemplate("test warp 1")),
        ).getOrThrow()
        assertTrue(WarpPath("/data1/test_warp_1") in compositor)
        assertTrue(WarpPath("/test_warp_1") in repo1)

        compositor.create(
            FolderPath("/data2/test_folder_2/"),
            FolderState(ComponentTemplate("test folder 2")),
        ).getOrThrow()
        assertTrue(FolderPath("/data2/test_folder_2/") in compositor)
        assertTrue(FolderPath("/test_folder_2/") in repo2)

        compositor.create(
            WarpPath("/data2/test_warp_2"),
            WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), ComponentTemplate("test warp 2")),
        ).getOrThrow()
        assertTrue(WarpPath("/data2/test_warp_2") in compositor)
        assertTrue(WarpPath("/test_warp_2") in repo2)

        compositor.create(
            FolderPath("/data2/data3/test_folder_3/"),
            FolderState(ComponentTemplate("test folder 3")),
        ).getOrThrow()
        assertTrue(FolderPath("/data2/data3/test_folder_3/") in compositor)
        assertTrue(FolderPath("/test_folder_3/") in repo3)

        compositor.create(
            WarpPath("/data2/data3/test_warp_3"),
            WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), ComponentTemplate("test warp 3")),
        ).getOrThrow()
        assertTrue(WarpPath("/data2/data3/test_warp_3") in compositor)
        assertTrue(WarpPath("/test_warp_3") in repo3)
    }

    @Test
    fun `test empty compositor`() {
        val compositor = Compositor(emptyList())
        assertThrows<MissingMountException> {
            runBlocking {
                compositor.create(FolderPath("/test_folder_0/"), FolderState(ComponentTemplate("test folder 0"))).getOrThrow()
            }
        }
    }

    @Test
    fun `test invalid compositor`() {
        val repo = Repository("repo", EphemeralStorage, Unit)

        val compositor = Compositor(
            listOf(
                CompositorMount(NodeParentPath.parse("/test/repo/").getOrThrow(), repo, RootPath) {},
            ),
        )
        assertThrows<MissingMountException> { runBlocking { compositor.load() } }
    }
}
