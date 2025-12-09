package net.voxelpi.varp.environment

import net.voxelpi.varp.environment.model.EnvironmentDefinition
import net.voxelpi.varp.repository.ephemeral.EphemeralRepositoryConfig
import net.voxelpi.varp.repository.ephemeral.EphemeralRepositoryType
import net.voxelpi.varp.tree.path.RootPath
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class VarpEnvironmentLoaderTest {

    @Test
    fun `test save to string`() {
        val definition = EnvironmentDefinition.environmentDefinition {
            repository("default", EphemeralRepositoryType, EphemeralRepositoryConfig) {
                mountedAt(RootPath)
            }
        }

        val loader = VarpEnvironmentLoader.withStandardTypes(emptyList())
        val actual = loader.saveToString(definition, Path(".")).getOrThrow()
        println(actual)
    }

    @Test
    fun `test cycle`() {
        val definition = EnvironmentDefinition.environmentDefinition {
            repository("default", EphemeralRepositoryType, EphemeralRepositoryConfig) {
                mountedAt(RootPath)
            }
        }
        val loader = VarpEnvironmentLoader.withStandardTypes(emptyList())

        val serialized = loader.saveToJson(definition, Path(".")).getOrThrow()
        val actual = loader.loadFromJson(serialized, Path(".")).getOrThrow()

        assertEquals(definition, actual)
    }
}
