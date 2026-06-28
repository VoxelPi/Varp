package net.voxelpi.varp.environment

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.voxelpi.varp.repository.EphemeralStorage
import net.voxelpi.varp.tree.state.treeState
import kotlin.test.Test
import kotlin.test.assertContentEquals

class VarpEnvironmentTest {

    @Test
    fun `test load from definition`() {
        val definition = EnvironmentDefinition.environmentDefinition {
            repository("main", EphemeralStorage) {
                mountedAt("/")

                defaultState = treeState("root") {
                    warp("warp1", Key.key("minecraft:overworld"), 2.0, 3.0, 4.0, 5f, 6f)
                    folder("folder1") {
                        warp("warp11", Key.key("minecraft:overworld"), 11.0, 3.0, 4.0, 5f, 6f)
                        warp("warp12", Key.key("minecraft:overworld"), 12.0, 3.0, 4.0, 5f, 6f)
                    }
                }
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
