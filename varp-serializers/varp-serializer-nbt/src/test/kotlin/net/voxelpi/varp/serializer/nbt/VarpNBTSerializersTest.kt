package net.voxelpi.varp.serializer.nbt

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.TagStringIO
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState
import kotlin.test.Test
import kotlin.test.assertEquals

class VarpNBTSerializersTest {

    @Test
    fun `test serialize WarpState`() {
        val expectedState = WarpState(
            MinecraftLocation(
                Key.key("minecraft:the_nether"),
                5.0,
                3.0,
                -2.3,
                -1.1f,
                3.4f,
            ),
            "A <rainbow>special</rainbow> warp",
            listOf("A warp that is", "very special"),
            setOf("first_tag", "second_tag"),
            mapOf("first_prop" to "test", "second_prop" to "test_2"),
        )

        val serialized = VarpNBTSerializers.serializeWarpState(expectedState)
        val actualState = VarpNBTSerializers.deserializeWarpState(serialized)

        val snbt = TagStringIO.builder().indent(0).build().asString(serialized)
        assertEquals(
            "{name:\"A <rainbow>special</rainbow> warp\",description:\"A warp that is\nvery special\",location:{x:5.0d,y:3.0d,z:-2.3d,pitch:3.4f,dimension:\"minecraft:the_nether\",yaw:-1.1f},properties:{first_prop:\"test\",second_prop:\"test_2\"},tags:[\"first_tag\",\"second_tag\"]}",
            snbt
        )
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `test serialize FolderState`() {
        val expectedState = FolderState(
            "A <rainbow>special</rainbow> folder",
            listOf("A folder that is", "very special"),
            setOf("first_tag", "second_tag"),
            mapOf("first_prop" to "test", "second_prop" to "test_2"),
        )

        val serialized = VarpNBTSerializers.serializeFolderState(expectedState)
        val actualState = VarpNBTSerializers.deserializeFolderState(serialized)

        val snbt = TagStringIO.builder().indent(0).build().asString(serialized)
        assertEquals(
            "{name:\"A <rainbow>special</rainbow> folder\",description:\"A folder that is\nvery special\",properties:{first_prop:\"test\",second_prop:\"test_2\"},tags:[\"first_tag\",\"second_tag\"]}",
            snbt
        )
        assertEquals(expectedState, actualState)
    }
}
