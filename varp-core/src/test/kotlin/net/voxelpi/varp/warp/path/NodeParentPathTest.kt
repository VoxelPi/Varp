package net.voxelpi.varp.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeParentPathTest {

    @Test
    fun `test parsing folder paths`() {
        // Valid paths.
        assertTrue(NodeParentPath.parse("/").isSuccess)
        assertTrue(NodeParentPath.parse("/folder/").isSuccess)
        assertTrue(NodeParentPath.parse("/folder1/folder/").isSuccess)
        assertTrue(NodeParentPath.parse("/folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(NodeParentPath.parse("").isFailure)
        assertTrue(NodeParentPath.parse("name").isFailure)
        assertTrue(NodeParentPath.parse("/warp").isFailure)
        assertTrue(NodeParentPath.parse("/folder1/warp").isFailure)
        assertTrue(NodeParentPath.parse("/folder1/folder2/warp").isFailure)
    }

    @Test
    fun `test path concatenation`() {
        val path1 = NodeParentPath.parse("/folder/").getOrThrow()

        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / (FolderPath("/folder2/folder3/") as NodePath))
        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / (FolderPath("/folder2/folder3/") as NodeParentPath))
        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / (FolderPath("/folder2/folder3/") as NodeChildPath))
        assertEquals(WarpPath("/folder/folder2/warp"), path1 / WarpPath("/folder2/warp"))
        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / FolderPath("/folder2/folder3/"))
        assertEquals(path1, path1 / RootPath)
    }
}
