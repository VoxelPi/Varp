package net.voxelpi.varp.environment

import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.environment.model.EnvironmentDefinition
import net.voxelpi.varp.repository.ephemeral.EphemeralRepositoryConfig
import net.voxelpi.varp.repository.ephemeral.EphemeralRepositoryType
import kotlin.test.Test
import kotlin.test.assertContentEquals

class VarpEnvironmentTest {

    @Test
    fun `test load from definition`() {
        val definition = EnvironmentDefinition.environmentDefinition {
            repository("main", EphemeralRepositoryType, EphemeralRepositoryConfig) {
                mountedAt("/")
            }
            repository("unused", EphemeralRepositoryType, EphemeralRepositoryConfig)
            repository("games_repo", EphemeralRepositoryType, EphemeralRepositoryConfig) {
                mountedAt("/games/")
            }
        }
        val environment = runBlocking {
            VarpEnvironment.environment(definition).getOrThrow()
        }

        assertContentEquals(environment.repositories.keys, listOf("main", "unused", "games_repo"))
        assertContentEquals(environment.compositor.mounts().map { it.path.toString() }, listOf("/", "/games/"))
    }
}
