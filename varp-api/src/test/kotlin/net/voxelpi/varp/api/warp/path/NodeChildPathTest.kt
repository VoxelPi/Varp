package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class NodeChildPathTest {

    @Test
    fun `test parsing folder paths`() {
        // Valid paths.
        assertTrue(NodeChildPath.parse("module:warp").isSuccess)
        assertTrue(NodeChildPath.parse("module:folder/").isSuccess)
        assertTrue(NodeChildPath.parse("module:folder1/warp").isSuccess)
        assertTrue(NodeChildPath.parse("module:folder1/folder/").isSuccess)
        assertTrue(NodeChildPath.parse("module:folder1/folder2/warp").isSuccess)
        assertTrue(NodeChildPath.parse("module:folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(NodeChildPath.parse("").isFailure)
        assertTrue(NodeChildPath.parse("name").isFailure)
        assertTrue(NodeChildPath.parse("module:").isFailure)
    }
}
