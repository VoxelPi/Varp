package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class NodeParentPathTest {

    @Test
    fun `test parsing folder paths`() {
        // Valid paths.
        assertTrue(NodeParentPath.parse("module:").isSuccess)
        assertTrue(NodeParentPath.parse("module:folder/").isSuccess)
        assertTrue(NodeParentPath.parse("module:folder1/folder/").isSuccess)
        assertTrue(NodeParentPath.parse("module:folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(NodeParentPath.parse("").isFailure)
        assertTrue(NodeParentPath.parse("name").isFailure)
        assertTrue(NodeParentPath.parse("module:warp").isFailure)
        assertTrue(NodeParentPath.parse("module:folder1/warp").isFailure)
        assertTrue(NodeParentPath.parse("module:folder1/folder2/warp").isFailure)
    }
}
