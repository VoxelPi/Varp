package net.voxelpi.varp.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class NodePathTest {

    @Test
    fun `test parsing paths`() {
        // Valid paths.
        assertTrue(NodePath.parse("/").isSuccess)
        assertTrue(NodePath.parse("/warp").isSuccess)
        assertTrue(NodePath.parse("/folder/").isSuccess)
        assertTrue(NodePath.parse("/folder1/warp").isSuccess)
        assertTrue(NodePath.parse("/folder1/folder/").isSuccess)
        assertTrue(NodePath.parse("/folder1/folder2/warp").isSuccess)
        assertTrue(NodePath.parse("/folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(NodePath.parse("").isFailure)
        assertTrue(NodePath.parse("name").isFailure)
    }
}
