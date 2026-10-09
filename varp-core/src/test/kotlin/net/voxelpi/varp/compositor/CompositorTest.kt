package net.voxelpi.varp.compositor

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.voxelpi.event.on
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.repository.EphemeralStorage
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.path.folderPath
import net.voxelpi.varp.tree.path.warpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState
import net.voxelpi.varp.tree.state.treeState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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

        assert(warpPath("a", "warp") in environment.compositor) { "Main warp not created" }
        assert(warpPath("b", "warp") in environment.compositor) { "Secondary warp not created" }
        assert(folderPath("a", "folder") in environment.compositor) { "Main folder not created" }
        assert(folderPath("b", "folder") in environment.compositor) { "Secondary folder not created" }
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

        environment.compositor.create(warpPath("a", "warp"), WarpState(Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f, "TEST")).getOrThrow()
        assert(warpPath("a", "warp") in environment.compositor) { "Main warp not created" }
        assert(warpPath("b", "warp") in environment.compositor) { "Secondary warp not created" }

        environment.compositor.create(folderPath("a", "folder"), FolderState("TEST")).getOrThrow()
        assert(folderPath("a", "folder") in environment.compositor) { "Main folder not created" }
        assert(folderPath("b", "folder") in environment.compositor) { "Secondary folder not created" }
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

        environment.compositor.delete(warpPath("a", "warp")).getOrThrow()
        assert(warpPath("a", "warp") !in environment.compositor) { "Main warp not deleted" }
        assert(warpPath("b", "warp") !in environment.compositor) { "Secondary warp not deleted" }
        environment.compositor.delete(folderPath("a", "folder")).getOrThrow()
        assert(folderPath("a", "folder") !in environment.compositor) { "Main folder not deleted" }
        assert(folderPath("b", "folder") !in environment.compositor) { "Secondary folder not deleted" }
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

        environment.compositor.move(warpPath("a", "warp"), warpPath("a", "stuff", "warp")).getOrThrow()
        assert(warpPath("a", "warp") !in environment.compositor) { "Main warp not deleted" }
        assert(warpPath("b", "warp") !in environment.compositor) { "Secondary warp not deleted" }
        assert(warpPath("a", "stuff", "warp") in environment.compositor) { "Main warp not created" }
        assert(warpPath("b", "stuff", "warp") in environment.compositor) { "Secondary warp not created" }
        environment.compositor.move(folderPath("a", "folder"), folderPath("a", "stuff", "folder")).getOrThrow()
        assert(folderPath("a", "folder") !in environment.compositor) { "Main folder not deleted" }
        assert(folderPath("b", "folder") !in environment.compositor) { "Secondary folder not deleted" }
        assert(folderPath("a", "stuff", "folder") in environment.compositor) { "Main folder not created" }
        assert(folderPath("b", "stuff", "folder") in environment.compositor) { "Secondary folder not created" }
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
        environment.compositor.update(warpPath("a", "warp"), newWarpState).getOrThrow()
        assertEquals(newWarpState, environment.compositor.state[warpPath("a", "warp")], "Main warp not modified")
        assertEquals(newWarpState, environment.compositor.state[warpPath("b", "warp")], "Secondary warp not modified")

        val newFolderState = FolderState(name = "new_folder")
        environment.compositor.update(folderPath("a", "folder"), newFolderState).getOrThrow()
        assertEquals(newFolderState, environment.compositor.state[folderPath("a", "folder")], "Main folder not modified")
        assertEquals(newFolderState, environment.compositor.state[folderPath("b", "folder")], "Secondary folder not modified")
    }

    @Test
    fun `moving a visible folder behind a mount deletes its representative`(): Unit = runBlocking {
        assertVisibilityTransition(initiallyHidden = false)
    }

    @Test
    fun `moving a hidden folder out of a mount creates its representative`(): Unit = runBlocking {
        assertVisibilityTransition(initiallyHidden = true)
    }

    private suspend fun assertVisibilityTransition(initiallyHidden: Boolean) {
        val environment = VarpEnvironment.build {
            repository("root", EphemeralStorage) {
                mountedAt("/")
            }
            repository("main", EphemeralStorage) {
                mountedAt("/a/")
                defaultState = treeState("main") {
                    folder("hidden") {
                        if (initiallyHidden) {
                            folder("subject") {
                                folder("child") {}
                                warp("warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f)
                            }
                        }
                    }
                    if (!initiallyHidden) {
                        folder("subject") {
                            folder("child") {}
                            warp("warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f)
                        }
                    }
                }
            }
            // This mount shadows /hidden/ in the main repository.
            repository("overlay", EphemeralStorage) {
                mountedAt("/a/hidden/")
                defaultState = treeState("overlay") {
                    warp("sentinel", Key.key("minecraft:overworld"), 9.0, 8.0, 7.0, 0f, 0f)
                }
            }
        }.getOrThrow()

        val compositor = environment.compositor
        val main = assertNotNull(compositor.mount(folderPath("a"))).repository
        val overlayMount = assertNotNull(compositor.mount(folderPath("a", "hidden")))
        val sentinelPath = warpPath("a", "hidden", "sentinel")
        val sentinelState = assertNotNull(compositor.state[sentinelPath])

        val visibleRepositoryPath = folderPath("subject")
        val hiddenRepositoryPath = folderPath("hidden", "subject")
        val source = if (initiallyHidden) hiddenRepositoryPath else visibleRepositoryPath
        val destination = if (initiallyHidden) visibleRepositoryPath else hiddenRepositoryPath

        val visibleFolder = folderPath("a", "subject")
        val visibleChild = folderPath("a", "subject", "child")
        val visibleWarp = warpPath("a", "subject", "warp")
        val expectedFolderState = assertNotNull(main.state[source])
        val expectedChildState = assertNotNull(main.state[source / folderPath("child")])
        val expectedWarpState = assertNotNull(main.state[source / warpPath("warp")])

        // Establish that the test starts with the expected projection.
        assertEquals(!initiallyHidden, visibleFolder in compositor)
        assertEquals(!initiallyHidden, visibleChild in compositor)
        assertEquals(!initiallyHidden, visibleWarp in compositor)

        // Subscribe after setup so loading events do not affect the assertions.
        val created = mutableListOf<FolderPath>()
        val deleted = mutableListOf<FolderPath>()
        val moved = mutableListOf<Pair<FolderPath, FolderPath>>()
        compositor.eventScope.on { event: FolderCreateEvent -> created += event.folder.path }
        compositor.eventScope.on { event: FolderDeleteEvent -> deleted += event.folder.path }
        compositor.eventScope.on { event: FolderPathChangeEvent -> moved += event.oldPath to event.newPath }

        // Move within the repository, bypassing Compositor.move().
        main.move(source, destination).getOrThrow()

        assertFalse(source in main, "Repository source must be removed")
        assertTrue(destination in main, "Repository destination must exist")
        assertEquals(expectedFolderState, main.state[destination])
        assertEquals(expectedChildState, main.state[destination / folderPath("child")])
        assertEquals(expectedWarpState, main.state[destination / warpPath("warp")])

        if (initiallyHidden) {
            assertEquals(expectedFolderState, compositor.state[visibleFolder], "Folder must become visible")
            assertEquals(expectedChildState, compositor.state[visibleChild], "Child folder must become visible")
            assertEquals(expectedWarpState, compositor.state[visibleWarp], "Warp must become visible")
            assertEquals(listOf(visibleFolder), created.filter { it == visibleFolder })
            assertTrue(deleted.isEmpty(), "Entering the view must not emit folder deletion")
        } else {
            assertFalse(visibleFolder in compositor, "Folder must become shadowed")
            assertFalse(visibleChild in compositor, "Child folder must become shadowed")
            assertFalse(visibleWarp in compositor, "Warp must become shadowed")
            assertEquals(listOf(visibleFolder), deleted.filter { it == visibleFolder })
            assertTrue(created.isEmpty(), "Becoming shadowed must not emit folder creation")
        }

        assertTrue(moved.isEmpty(), "Only one endpoint is visible, so no compositor move event is expected")
        assertFalse(folderPath("a", "hidden", "subject") in compositor)
        assertFalse(folderPath("a", "hidden", "subject", "child") in compositor)
        assertFalse(warpPath("a", "hidden", "subject", "warp") in compositor)
        assertEquals(overlayMount, compositor.mount(folderPath("a", "hidden")))
        assertEquals(sentinelState, compositor.state[sentinelPath], "Overlay content must remain unchanged")
    }
}
