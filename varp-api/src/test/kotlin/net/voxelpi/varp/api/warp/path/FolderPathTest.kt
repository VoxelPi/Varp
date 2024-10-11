package net.voxelpi.varp.api.warp.path

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
}
