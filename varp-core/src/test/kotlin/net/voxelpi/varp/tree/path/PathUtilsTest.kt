package net.voxelpi.varp.tree.path

import kotlin.test.Test
import kotlin.test.assertEquals

class PathUtilsTest {

    @Test
    fun `test top level paths 1`() {
        val paths = listOf(
            FolderPath("/test/test2/test3/"),
            FolderPath("/test/test2/test1/"),
            FolderPath("/test/test2/"),
            FolderPath("/test/test3/"),
        )

        assertEquals(
            setOf(
                FolderPath("/test/test2/"),
                FolderPath("/test/test3/"),
            ),
            topLevelPaths(paths),
        )
    }

    @Test
    fun `test top level paths 2`() {
        val paths = listOf(
            FolderPath("/test/test2/test3/"),
            FolderPath("/test/test2/test1/"),
            FolderPath("/test/test2/"),
            FolderPath("/test/test3/"),
            RootPath
        )

        assertEquals(
            setOf(
                RootPath,
            ),
            topLevelPaths(paths),
        )
    }
}
