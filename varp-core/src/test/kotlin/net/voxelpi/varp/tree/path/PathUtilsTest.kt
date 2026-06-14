package net.voxelpi.varp.tree.path

import net.voxelpi.varp.util.Movement
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

    @Test
    fun `test resolve local movement`() {
        val result = resolveLocalMovements(
            listOf(
                Movement(
                    from = FolderPath("/a/"),
                    to = FolderPath("/b/"),
                ),
                Movement(
                    from = FolderPath("/a/c/"),
                    to = FolderPath("/a/d/"),
                ),
            )
        )

        assertEquals(
            listOf<Movement<FolderPath?>>(
                Movement(
                    from = FolderPath("/a/"),
                    to = FolderPath("/b/"),
                ),
                Movement(
                    from = FolderPath("/a/c/"),
                    to = FolderPath("/b/d/"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `resolves deeply nested movement through nearest parent movement`() {
        val result = resolveLocalMovements(
            listOf(
                Movement(
                    from = FolderPath("/a/"),
                    to = FolderPath("/b/"),
                ),
                Movement(
                    from = FolderPath("/a/c/"),
                    to = FolderPath("/a/d/"),
                ),
                Movement(
                    from = FolderPath("/a/c/e/"),
                    to = FolderPath("/a/c/f/"),
                ),
            )
        )

        assertEquals(
            listOf<Movement<FolderPath?>>(
                Movement(
                    from = FolderPath("/a/"),
                    to = FolderPath("/b/"),
                ),
                Movement(
                    from = FolderPath("/a/c/"),
                    to = FolderPath("/b/d/"),
                ),
                Movement(
                    from = FolderPath("/a/c/e/"),
                    to = FolderPath("/b/d/f/"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `resolves movement to deletion when parent is deleted`() {
        val result = resolveLocalMovements(
            listOf(
                Movement(
                    from = FolderPath("/a/"),
                    to = null,
                ),
                Movement(
                    from = FolderPath("/a/c/"),
                    to = FolderPath("/a/d/"),
                ),
            )
        )

        assertEquals(
            listOf<Movement<FolderPath?>>(
                Movement(
                    from = FolderPath("/a/"),
                    to = null,
                ),
                Movement(
                    from = FolderPath("/a/c/"),
                    to = null,
                ),
            ),
            result,
        )
    }
}
