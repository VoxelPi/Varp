package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ModulePathTest {

    @Test
    fun `test parsing module paths`() {
        // Valid paths.
        assertTrue(ModulePath.parse("module:").isSuccess)

        // Invalid paths.
        assertTrue(ModulePath.parse("").isFailure)
        assertTrue(ModulePath.parse("name").isFailure)
        assertTrue(ModulePath.parse("module:warp").isFailure)
        assertTrue(ModulePath.parse("module:folder1/").isFailure)
        assertTrue(ModulePath.parse("module:folder1/warp").isFailure)
        assertTrue(ModulePath.parse("module:folder1/folder2/").isFailure)
        assertTrue(ModulePath.parse("module:folder1/folder2/warp").isFailure)
    }
}
