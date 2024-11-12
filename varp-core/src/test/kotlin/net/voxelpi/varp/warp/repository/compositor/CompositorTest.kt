package net.voxelpi.varp.warp.repository.compositor

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.ephemeral.EphemeralRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CompositorTest {

    @Test
    fun `test compositor create`(): Unit = runBlocking {
        val repo0 = EphemeralRepository("repo0")
        val repo1 = EphemeralRepository("repo1")
        val repo2 = EphemeralRepository("repo2")
        val repo3 = EphemeralRepository("repo3")

        val compositor = Compositor(
            "main",
            CompositorConfig(
                listOf(
                    CompositorMount(NodeParentPath.parse("/").getOrThrow(), repo0),
                    CompositorMount(NodeParentPath.parse("/data1/").getOrThrow(), repo1),
                    CompositorMount(NodeParentPath.parse("/data2/").getOrThrow(), repo2),
                    CompositorMount(NodeParentPath.parse("/data2/data3/").getOrThrow(), repo3),
                ),
            ),
        )

        compositor.create(FolderPath("/test_folder_0/"), FolderState(Component.text("test folder 0")))
        assertTrue(compositor.tree.exists(FolderPath("/test_folder_0/")))
        assertTrue(repo0.tree.exists(FolderPath("/test_folder_0/")))

        compositor.create(WarpPath("/test_warp_0"), WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), Component.text("test warp 0")))
        assertTrue(compositor.tree.exists(WarpPath("/test_warp_0")))
        assertTrue(repo0.tree.exists(WarpPath("/test_warp_0")))

        compositor.create(FolderPath("/data1/test_folder_1/"), FolderState(Component.text("test folder 1")))
        assertTrue(compositor.tree.exists(FolderPath("/data1/test_folder_1/")))
        assertTrue(repo1.tree.exists(FolderPath("/test_folder_1/")))

        compositor.create(WarpPath("/data1/test_warp_1"), WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), Component.text("test warp 1")))
        assertTrue(compositor.tree.exists(WarpPath("/data1/test_warp_1")))
        assertTrue(repo1.tree.exists(WarpPath("/test_warp_1")))

        compositor.create(FolderPath("/data2/test_folder_2/"), FolderState(Component.text("test folder 2")))
        assertTrue(compositor.tree.exists(FolderPath("/data2/test_folder_2/")))
        assertTrue(repo2.tree.exists(FolderPath("/test_folder_2/")))

        compositor.create(WarpPath("/data2/test_warp_2"), WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), Component.text("test warp 2")))
        assertTrue(compositor.tree.exists(WarpPath("/data2/test_warp_2")))
        assertTrue(repo2.tree.exists(WarpPath("/test_warp_2")))

        compositor.create(FolderPath("/data2/data3/test_folder_3/"), FolderState(Component.text("test folder 3")))
        assertTrue(compositor.tree.exists(FolderPath("/data2/data3/test_folder_3/")))
        assertTrue(repo3.tree.exists(FolderPath("/test_folder_3/")))

        compositor.create(WarpPath("/data2/data3/test_warp_3"), WarpState(MinecraftLocation(Key.key("varp:test"), 50.0, 40.0, 30.0, 20f, 10f), Component.text("test warp 3")))
        assertTrue(compositor.tree.exists(WarpPath("/data2/data3/test_warp_3")))
        assertTrue(repo3.tree.exists(WarpPath("/test_warp_3")))
    }
}
