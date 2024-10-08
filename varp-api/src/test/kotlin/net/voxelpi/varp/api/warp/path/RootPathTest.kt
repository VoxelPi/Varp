package net.voxelpi.varp.api.warp.path

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RootPathTest {

    @Test
    fun `test parsing module paths`() {
        assertEquals("/", RootPath.value)
    }
}
