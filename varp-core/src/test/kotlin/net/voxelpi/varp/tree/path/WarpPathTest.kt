package net.voxelpi.varp.tree.path

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WarpPathTest {

    @Test
    fun `test parsing warp paths`() {
        // Valid paths.
        assertTrue(WarpPath.parse("/warp").isSuccess)
        assertTrue(WarpPath.parse("/folder1/warp").isSuccess)
        assertTrue(WarpPath.parse("/folder1/folder2/warp").isSuccess)

        // Invalid paths.
        assertTrue(WarpPath.parse("").isFailure)
        assertTrue(WarpPath.parse("name").isFailure)
        assertTrue(WarpPath.parse("/").isFailure)
        assertTrue(WarpPath.parse("/folder1/").isFailure)
        assertTrue(WarpPath.parse("/folder1/folder2/").isFailure)
    }

    @Test
    fun `test is sub path`() {
        val path = WarpPath("/folder1/folder2/warp")
        val folder1Path = FolderPath("/folder1/")
        val folder2Path = FolderPath("/folder1/folder2/")
        val folder3Path = FolderPath("/folder1/folder3/")

        // Sub paths.
        assertTrue(folder1Path.isSubPathOf(path))
        assertTrue(folder2Path.isSubPathOf(path))
        assertTrue(RootPath.isSubPathOf(path))

        // Not sub paths.
        assertFalse(folder3Path.isSubPathOf(path))
    }

    @Test
    fun `test relative paths`() {
        val path = WarpPath("/folder1/folder2/warp")
        val folder1Path = FolderPath("/folder1/")
        val folder2Path = FolderPath("/folder1/folder2/")
        val folder3Path = FolderPath("/folder1/folder3/")

        // Relative to sub path.
        assertEquals(WarpPath("/folder2/warp"), path.relativeTo(folder1Path))
        assertEquals(WarpPath("/warp"), path.relativeTo(folder2Path))

        // Relative to path that is not a sub path.
        assertEquals(null, path.relativeTo(folder3Path))
    }

    @Test
    fun `test id`() {
        assertEquals("warp1", WarpPath("/warp1").id)
        assertEquals("warp2", WarpPath("/folder1/warp2").id)
        assertEquals("warp3", WarpPath("/folder1/folder2/warp3").id)
    }

    @Test
    fun `test parent`() {
        val root = RootPath
        val path = WarpPath("/folder1/folder2/warp")
        val path2 = FolderPath("/folder1/folder2/")

        assertEquals(path2, path.parent)
    }
}
