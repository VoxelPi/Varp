package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class WarpPathTest {

    @Test
    fun `test parsing warp paths`() {
        // Valid paths.
        assertTrue(WarpPath.parse("module:warp").isSuccess)
        assertTrue(WarpPath.parse("module:folder1/warp").isSuccess)
        assertTrue(WarpPath.parse("module:folder1/folder2/warp").isSuccess)

        // Invalid paths.
        assertTrue(WarpPath.parse("").isFailure)
        assertTrue(WarpPath.parse("name").isFailure)
        assertTrue(WarpPath.parse("module:").isFailure)
        assertTrue(WarpPath.parse("module:folder1/").isFailure)
        assertTrue(WarpPath.parse("module:folder1/folder2/").isFailure)
    }
}
