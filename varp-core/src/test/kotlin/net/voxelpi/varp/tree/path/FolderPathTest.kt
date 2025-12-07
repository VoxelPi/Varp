package net.voxelpi.varp.tree.path

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FolderPathTest {

    @Test
    fun `test parsing folder paths`() {
        // Valid paths.
        assertTrue(FolderPath.parse("/folder/").isSuccess)
        assertTrue(FolderPath.parse("/folder1/folder/").isSuccess)
        assertTrue(FolderPath.parse("/folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(FolderPath.parse("").isFailure)
        assertTrue(FolderPath.parse("name").isFailure)
        assertTrue(FolderPath.parse("/").isFailure)
        assertTrue(FolderPath.parse("/folder").isFailure)
        assertTrue(FolderPath.parse("/folder1/folder").isFailure)
        assertTrue(FolderPath.parse("/folder1/folder2/folder").isFailure)
    }

    @Test
    fun `test is sub path`() {
        val path = FolderPath("/folder1/folder2/folder/")
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
        val path = FolderPath("/folder1/folder2/folder/")
        val folder1Path = FolderPath("/folder1/")
        val folder2Path = FolderPath("/folder1/folder2/")
        val folder3Path = FolderPath("/folder1/folder3/")

        // Relative to sub path.
        assertEquals(NodeParentPath.parse("/folder2/folder/").getOrThrow(), path.relativeTo(folder1Path))
        assertEquals(NodeParentPath.parse("/folder/").getOrThrow(), path.relativeTo(folder2Path))
        assertEquals(NodeParentPath.parse("/").getOrThrow(), path.relativeTo(path))

        // Relative to path that is not a sub path.
        assertEquals(null, path.relativeTo(folder3Path))
    }

    @Test
    fun `test id`() {
        assertEquals("test_test", FolderPath("/test_test/").id)
        assertEquals("test_2_test", FolderPath("/folder1/test_2_test/").id)
        assertEquals("folder", FolderPath("/folder1/folder2/folder/").id)
    }

    @Test
    fun `test parent`() {
        val root = RootPath
        val path = FolderPath("/folder1/folder2/folder/")
        val path1 = FolderPath("/folder1/")
        val path2 = FolderPath("/folder1/folder2/")
        val path3 = FolderPath("/folder1/folder3/")

        assertEquals(path2, path.parent)
        assertEquals(path1, path2.parent)
        assertEquals(root, path1.parent)
    }

    @Test
    fun `test path concatenation`() {
        val path1 = FolderPath.parse("/folder/").getOrThrow()

        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / (FolderPath("/folder2/folder3/") as NodePath))
        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / (FolderPath("/folder2/folder3/") as NodeParentPath))
        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / (FolderPath("/folder2/folder3/") as NodeChildPath))
        assertEquals(WarpPath("/folder/folder2/warp"), path1 / WarpPath("/folder2/warp"))
        assertEquals(FolderPath("/folder/folder2/folder3/"), path1 / FolderPath("/folder2/folder3/"))
        assertEquals(path1, path1 / RootPath)
    }
}
