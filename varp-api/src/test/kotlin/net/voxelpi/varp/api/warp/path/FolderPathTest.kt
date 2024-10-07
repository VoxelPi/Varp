package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class FolderPathTest {

    @Test
    fun `test parsing folder paths`() {
        // Valid paths.
        assertTrue(FolderPath.parse("module:folder/").isSuccess)
        assertTrue(FolderPath.parse("module:folder1/folder/").isSuccess)
        assertTrue(FolderPath.parse("module:folder1/folder2/folder/").isSuccess)

        // Invalid paths.
        assertTrue(FolderPath.parse("").isFailure)
        assertTrue(FolderPath.parse("name").isFailure)
        assertTrue(FolderPath.parse("module:").isFailure)
        assertTrue(FolderPath.parse("module:folder").isFailure)
        assertTrue(FolderPath.parse("module:folder1/folder").isFailure)
        assertTrue(FolderPath.parse("module:folder1/folder2/folder").isFailure)
    }
}
