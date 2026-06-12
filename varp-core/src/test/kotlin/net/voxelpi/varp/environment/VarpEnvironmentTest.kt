package net.voxelpi.varp.environment

import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.repository.EphemeralStorage
import kotlin.test.Test
import kotlin.test.assertContentEquals

class VarpEnvironmentTest {

    @Test
    fun `test load from definition`() {
        val definition = EnvironmentDefinition.environmentDefinition {
            repository("main", EphemeralStorage) {
                mountedAt("/")
            }
            repository("unused", EphemeralStorage)
            repository("games_repo", EphemeralStorage) {
                mountedAt("/games/")
            }
        }
        val environment = runBlocking {
            VarpEnvironment.environment(definition).getOrThrow()
        }

        assertContentEquals(environment.repositories.keys, listOf("main", "unused", "games_repo"))
        assertContentEquals(environment.compositor.mounts().map { it.targetPath.toString() }, listOf("/", "/games/"))
    }
}
