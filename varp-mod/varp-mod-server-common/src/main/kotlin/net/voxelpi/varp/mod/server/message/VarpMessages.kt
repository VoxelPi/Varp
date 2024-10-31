package net.voxelpi.varp.mod.server.message

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.Moonshine
import net.kyori.moonshine.annotation.Message
import net.kyori.moonshine.annotation.Placeholder
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy
import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.server.message.placeholder.ComponentPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.NumberPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.StringPlaceholderResolver
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.Root
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.path.WarpPath
import org.spongepowered.configurate.ConfigurationNode

interface VarpMessages {

    @Message("prefix")
    fun prefix(): Component

    // region client link messages

    @Message("client.support_enabled")
    fun sendClientSupportEnabled(
        @Receiver receiver: Audience,
        @Placeholder("client") clientInformation: VarpClientInformation,
    )

    @Message("client.error.no_support")
    fun sendErrorNoClientSupport(
        @Receiver receiver: Audience,
    )

    @Message("client.error.incompatible_protocol_version")
    fun sendClientErrorIncompatibleProtocolVersion(
        @Receiver receiver: Audience,
        @Placeholder("client") client: VarpClientInformation,
        @Placeholder("server") server: VarpServerInformation,
    )

    // endregion

    // region warp messages

    @Message("warp.create")
    fun sendWarpCreate(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
    )

    @Message("warp.delete")
    fun sendWarpDelete(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
    )

    @Message("warp.copy")
    fun sendWarpCopy(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
        @Placeholder("src") src: WarpPath,
        @Placeholder("dst") dst: WarpPath,
    )

    @Message("warp.move")
    fun sendWarpMove(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
        @Placeholder("src") src: WarpPath,
        @Placeholder("dst") dst: WarpPath,
    )

    @Message("warp.edit")
    fun sendWarpEdit(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
    )

    @Message("warp.teleport.self")
    fun sendWarpTeleportSelf(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
    )

    // endregion

    // region folder messages

    @Message("folder.create")
    fun sendFolderCreate(
        @Receiver receiver: Audience,
        @Placeholder("folder") folder: Folder,
    )

    @Message("folder.delete")
    fun sendFolderDelete(
        @Receiver receiver: Audience,
        @Placeholder("folder") folder: Folder,
    )

    @Message("folder.copy")
    fun sendFolderCopy(
        @Receiver receiver: Audience,
        @Placeholder("folder") folder: Folder,
        @Placeholder("src") src: FolderPath,
        @Placeholder("dst") dst: FolderPath,
    )

    @Message("folder.move")
    fun sendFolderMove(
        @Receiver receiver: Audience,
        @Placeholder("folder") folder: Folder,
        @Placeholder("src") src: FolderPath,
        @Placeholder("dst") dst: FolderPath,
    )

    @Message("folder.edit")
    fun sendFolderEdit(
        @Receiver receiver: Audience,
        @Placeholder("folder") folder: Folder,
    )

    // endregion

    // region root messages

    @Message("root.edit")
    fun sendRootEdit(
        @Receiver receiver: Audience,
        @Placeholder("root") root: Root,
    )

    // endregion

    // region common error messages

    @Message("error.error_occurred")
    fun sendErrorGeneric(
        @Receiver receiver: Audience,
    )

    @Message("error.only_players")
    fun sendErrorOnlyPlayers(
        @Receiver receiver: Audience,
    )

    @Message("error.no_permission")
    fun sendErrorNoPermission(
        @Receiver receiver: Audience,
    )

    @Message("error.warp_path_unresolved")
    fun sendErrorWarpPathUnresolved(
        @Receiver receiver: Audience,
        @Placeholder("path") path: WarpPath,
    )

    @Message("error.folder_path_unresolved")
    fun sendErrorFolderPathUnresolved(
        @Receiver receiver: Audience,
        @Placeholder("path") path: FolderPath,
    )

    @Message("error.warp_already_exists")
    fun sendErrorWarpAlreadyExists(
        @Receiver receiver: Audience,
        @Placeholder("path") path: WarpPath,
    )

    @Message("error.folder_already_exists")
    fun sendErrorFolderAlreadyExists(
        @Receiver receiver: Audience,
        @Placeholder("path") path: FolderPath,
    )

    @Message("error.node_already_exists")
    fun sendErrorNodeAlreadyExists(
        @Receiver receiver: Audience,
        @Placeholder("path") path: NodePath,
    )

    @Message("error.move_folder_into_child")
    fun sendErrorMoveFolderIntoChild(
        @Receiver receiver: Audience,
    )

    // endregion

    companion object {

        fun create(node: ConfigurationNode): VarpMessages {
            return Moonshine.builder<VarpMessages, Audience>(TypeToken.get(VarpMessages::class.java))
                .receiverLocatorResolver(AnnotationReceiverLocatorResolver, 0)
                .sourced(ConfigurateMessageSource(node))
                .rendered(
                    MiniMessageMessageRenderer(
                        listOf(
                            net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("prefix", node.node("prefix").getString("")),
                        )
                    )
                )
                .sent(AudienceMessageSender)
                .resolvingWithStrategy(StandardPlaceholderResolverStrategy(StandardSupertypeThenInterfaceSupertypeStrategy(true)))
                .apply {
                    weightedPlaceholderResolver(Component::class.java, ComponentPlaceholderResolver, 1)
                    weightedPlaceholderResolver(String::class.java, StringPlaceholderResolver, 1)
                    weightedPlaceholderResolver(Number::class.java, NumberPlaceholderResolver, 1)
                }
                .create(this::class.java.classLoader)
        }
    }
}
