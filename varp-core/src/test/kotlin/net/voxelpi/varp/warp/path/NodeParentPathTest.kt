package net.voxelpi.varp.warp.path

import org.junit.jupiter.api.Test
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
}
