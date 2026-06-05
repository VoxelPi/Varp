package net.voxelpi.varp.tree.state

import net.kyori.adventure.key.Key
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeStateTest {

    @Test
    fun `test empty treeState`() {
        val state = treeState(name = "my_root") {}

        assertEquals(
            mapOf(),
            state.warps
        )

        assertEquals(
            mapOf(),
            state.folders
        )
    }

    @Test
    fun `test treeState warp`() {
        val state = treeState(name = "my_root") {
            warp("my_warp", Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4F, 5F)
        }

        assertEquals(
            mapOf(
                WarpPath("/my_warp") to WarpState(MinecraftLocation(Key.key("minecraft:overworld"), 1.0, 2.0, 3.0, 4f, 5f), ComponentTemplate("my_warp")),
            ),
            state.warps
        )

        assertEquals(
            mapOf(),
            state.folders
        )
    }

    @Test
    fun `test treeState folder`() {
        val state = treeState(name = "my_root") {
            folder("my_folder") {}
        }

        assertEquals(
            mapOf(),
            state.warps
        )

        assertEquals(
            mapOf(
                FolderPath("/my_folder/") to FolderState(ComponentTemplate("my_folder")),
            ),
            state.folders
        )
    }

    @Test
    fun `test treeState`() {
        val state = treeState(name = "my_root") {
            warp("warp_0", Key.key("minecraft:overworld"), 2.0, 3.0, 4.0)
            warp("warp_1", Key.key("minecraft:overworld"), 1.0, -1.0, -2.3)
            folder("folder_0") {
                warp("warp_1", Key.key("minecraft:overworld"), 1.0, 1.0, -2.3)
                folder("folder_1") {
                    warp("warp_1", Key.key("minecraft:overworld"), 1.0, -1.0, 2.3)
                }
                folder("folder_2") {
                    warp("warp_1", Key.key("minecraft:overworld"), 1.0, -1.0, 2.3)
                }
            }
        }

        assertEquals(5, state.warps.size)
        assertEquals(3, state.folders.size)

        assertEquals(
            mapOf(
                WarpPath("/warp_0") to WarpState(MinecraftLocation(Key.key("minecraft:overworld"), 2.0, 3.0, 4.0, 0f, 0f), ComponentTemplate("warp_0")),
                WarpPath("/warp_1") to WarpState(MinecraftLocation(Key.key("minecraft:overworld"), 1.0, -1.0, -2.3, 0f, 0f), ComponentTemplate("warp_1")),
                WarpPath("/folder_0/warp_1") to WarpState(MinecraftLocation(Key.key("minecraft:overworld"), 1.0, 1.0, -2.3, 0f, 0f), ComponentTemplate("warp_1")),
                WarpPath("/folder_0/folder_1/warp_1") to WarpState(MinecraftLocation(Key.key("minecraft:overworld"), 1.0, -1.0, 2.3, 0f, 0f), ComponentTemplate("warp_1")),
                WarpPath("/folder_0/folder_2/warp_1") to WarpState(MinecraftLocation(Key.key("minecraft:overworld"), 1.0, -1.0, 2.3, 0f, 0f), ComponentTemplate("warp_1")),
            ),
            state.warps
        )

        assertEquals(
            mapOf(
                FolderPath("/folder_0/") to FolderState(ComponentTemplate("folder_0")),
                FolderPath("/folder_0/folder_1/") to FolderState(ComponentTemplate("folder_1")),
                FolderPath("/folder_0/folder_2/") to FolderState(ComponentTemplate("folder_2")),
            ),
            state.folders
        )
    }
}
