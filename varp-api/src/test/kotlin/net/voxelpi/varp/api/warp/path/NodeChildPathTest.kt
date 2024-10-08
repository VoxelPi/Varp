package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class NodeChildPathTest {

    @Test
    fun `test parsing folder paths`() {
        // Valid paths.
        assertTrue(NodeChildPath.parse("/warp").isSuccess)
        assertTrue(NodeChildPath.parse("/folder/").isSuccess)
        assertTrue(NodeChildPath.parse("/folder1/warp").isSuccess)
        assertTrue(NodeChildPath.parse("/folder1/folder/").isSuccess)
        assertTrue(NodeChildPath.parse("/folder1/folder2/warp").isSuccess)
        assertTrue(NodeChildPath.parse("/folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(NodeChildPath.parse("").isFailure)
        assertTrue(NodeChildPath.parse("name").isFailure)
        assertTrue(NodeChildPath.parse("/").isFailure)
    }
}
