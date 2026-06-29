package net.voxelpi.varp.compositor

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.repository.EphemeralStorage
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState
import net.voxelpi.varp.tree.state.treeState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
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
                CompositorMount(NodeParentPath.parse("/").getOrThrow(), repo0, RootPath),
                CompositorMount(NodeParentPath.parse("/data1/").getOrThrow(), repo1, RootPath),
                CompositorMount(NodeParentPath.parse("/data2/").getOrThrow(), repo2, RootPath),
                CompositorMount(NodeParentPath.parse("/data2/data3/").getOrThrow(), repo3, RootPath),
            ),
        )
        compositor.load()

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
                CompositorMount(NodeParentPath.parse("/test/repo/").getOrThrow(), repo, RootPath),
            ),
        )
        assertThrows<MissingMountException> { runBlocking { compositor.load() } }
    }

    @Test
    fun `test init all representatives`() = runBlocking {
        val environment = VarpEnvironment.build {
            repository("root", EphemeralStorage) {
                mountedAt("/")
            }
            repository("main", EphemeralStorage) {
                mountedAt("/a/")
                mountedAt("/b/")

                defaultState = treeState("root") {
                    warp("warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f)
                    folder("folder") {}
                }
            }
        }.getOrThrow()

        assert(RootPath.folder("a").warp("warp") in environment.compositor) { "Main warp not created" }
        assert(RootPath.folder("b").warp("warp") in environment.compositor) { "Secondary warp not created" }
        assert(RootPath.folder("a").folder("folder") in environment.compositor) { "Main folder not created" }
        assert(RootPath.folder("b").folder("folder") in environment.compositor) { "Secondary folder not created" }
    }

    @Test
    fun `test create all representatives`() = runBlocking {
        val environment = VarpEnvironment.build {
            repository("root", EphemeralStorage) {
                mountedAt("/")
            }
            repository("main", EphemeralStorage) {
                mountedAt("/a/")
                mountedAt("/b/")
            }
        }.getOrThrow()

        environment.compositor.create(RootPath.folder("a").warp("warp"), WarpState(Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f, "TEST")).getOrThrow()
        assert(RootPath.folder("a").warp("warp") in environment.compositor) { "Main warp not created" }
        assert(RootPath.folder("b").warp("warp") in environment.compositor) { "Secondary warp not created" }

        environment.compositor.create(RootPath.folder("a").folder("folder"), FolderState("TEST")).getOrThrow()
        assert(RootPath.folder("a").folder("folder") in environment.compositor) { "Main folder not created" }
        assert(RootPath.folder("b").folder("folder") in environment.compositor) { "Secondary folder not created" }
    }

    @Test
    fun `test delete all representatives`() = runBlocking {
        val environment = VarpEnvironment.build {
            repository("root", EphemeralStorage) {
                mountedAt("/")
            }
            repository("main", EphemeralStorage) {
                mountedAt("/a/")
                mountedAt("/b/")

                defaultState = treeState("root") {
                    warp("warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f)
                    folder("folder") {}
                }
            }
        }.getOrThrow()

        environment.compositor.delete(RootPath.folder("a").warp("warp")).getOrThrow()
        assert(RootPath.folder("a").warp("warp") !in environment.compositor) { "Main warp not deleted" }
        assert(RootPath.folder("b").warp("warp") !in environment.compositor) { "Secondary warp not deleted" }
        environment.compositor.delete(RootPath.folder("a").folder("folder")).getOrThrow()
        assert(RootPath.folder("a").folder("folder") !in environment.compositor) { "Main folder not deleted" }
        assert(RootPath.folder("b").folder("folder") !in environment.compositor) { "Secondary folder not deleted" }
    }

    @Test
    fun `test move all representatives`() = runBlocking {
        val environment = VarpEnvironment.build {
            repository("root", EphemeralStorage) {
                mountedAt("/")
            }
            repository("main", EphemeralStorage) {
                mountedAt("/a/")
                mountedAt("/b/")

                defaultState = treeState("root") {
                    warp("warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f)
                    folder("folder") {}
                    folder("stuff") {}
                }
            }
        }.getOrThrow()

        environment.compositor.move(RootPath.folder("a").warp("warp"), RootPath.folder("a").folder("stuff").warp("warp")).getOrThrow()
        assert(RootPath.folder("a").warp("warp") !in environment.compositor) { "Main warp not deleted" }
        assert(RootPath.folder("b").warp("warp") !in environment.compositor) { "Secondary warp not deleted" }
        assert(RootPath.folder("a").folder("stuff").warp("warp") in environment.compositor) { "Main warp not created" }
        assert(RootPath.folder("b").folder("stuff").warp("warp") in environment.compositor) { "Secondary warp not created" }
        environment.compositor.move(RootPath.folder("a").folder("folder"), RootPath.folder("a").folder("stuff").folder("folder")).getOrThrow()
        assert(RootPath.folder("a").folder("folder") !in environment.compositor) { "Main folder not deleted" }
        assert(RootPath.folder("b").folder("folder") !in environment.compositor) { "Secondary folder not deleted" }
        assert(RootPath.folder("a").folder("stuff").folder("folder") in environment.compositor) { "Main folder not created" }
        assert(RootPath.folder("b").folder("stuff").folder("folder") in environment.compositor) { "Secondary folder not created" }
    }

    @Test
    fun `test update all representatives`() = runBlocking {
        val environment = VarpEnvironment.build {
            repository("root", EphemeralStorage) {
                mountedAt("/")
            }
            repository("main", EphemeralStorage) {
                mountedAt("/a/")
                mountedAt("/b/")

                defaultState = treeState("root") {
                    warp("warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f, name = "old_warp")
                    folder("folder", name = "old_folder") {}
                }
            }
        }.getOrThrow()

        val newWarpState = WarpState(Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f, name = "new_warp")
        environment.compositor.update(RootPath.folder("a").warp("warp"), newWarpState).getOrThrow()
        assertEquals(newWarpState, environment.compositor.state[RootPath.folder("a").warp("warp")], "Main warp not modified")
        assertEquals(newWarpState, environment.compositor.state[RootPath.folder("b").warp("warp")], "Secondary warp not modified")

        val newFolderState = FolderState(name = "new_folder")
        environment.compositor.update(RootPath.folder("a").folder("folder"), newFolderState).getOrThrow()
        assertEquals(newFolderState, environment.compositor.state[RootPath.folder("a").folder("folder")], "Main folder not modified")
        assertEquals(newFolderState, environment.compositor.state[RootPath.folder("b").folder("folder")], "Secondary folder not modified")
    }
}
