package net.voxelpi.varp.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RootPathTest {

    @Test
    fun `test parsing module paths`() {
        assertEquals("/", RootPath.value)
    }

    @Test
    fun `test is sub path`() {
        val folder1Path = FolderPath("/folder1/")
        val folder2Path = FolderPath("/folder1/folder2/")

        // Sub paths
        assertTrue(RootPath.isSubPathOf(RootPath))

        // Not sub paths.
        assertFalse(folder1Path.isSubPathOf(RootPath))
        assertFalse(folder2Path.isSubPathOf(RootPath))
    }

    @Test
    fun `test relative paths`() {
        val path = RootPath
        val folder1Path = FolderPath("/folder1/")

        assertEquals(null, path.relativeTo(folder1Path))
        assertEquals(RootPath, path.relativeTo(RootPath))
    }

    @Test
    fun `test path concatenation`() {
        assertEquals(FolderPath("/folder2/folder3/"), RootPath / (FolderPath("/folder2/folder3/") as NodePath))
        assertEquals(FolderPath("/folder2/folder3/"), RootPath / (FolderPath("/folder2/folder3/") as NodeParentPath))
        assertEquals(FolderPath("/folder2/folder3/"), RootPath / (FolderPath("/folder2/folder3/") as NodeChildPath))
        assertEquals(WarpPath("/folder2/warp"), RootPath / WarpPath("/folder2/warp"))
        assertEquals(FolderPath("/folder2/folder3/"), RootPath / FolderPath("/folder2/folder3/"))
        assertEquals(RootPath, RootPath / RootPath)
    }
}
