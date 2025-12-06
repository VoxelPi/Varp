package net.voxelpi.varp.mod.server.message

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.moonshine.Moonshine
import net.kyori.moonshine.annotation.Message
import net.kyori.moonshine.annotation.Placeholder
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import net.voxelpi.varp.mod.server.message.placeholder.ComponentPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.FolderPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.FolderStatePlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.KeyPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.MinecraftLocationPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.NodePathPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.NumberPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.RootPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.StringPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.VarpClientInformationPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.VarpServerInformationPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.VarpServerPlayerPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.WarpPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.WarpStatePlaceholderResolver
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.Root
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import org.spongepowered.configurate.ConfigurationNode

interface VarpMessages {

    @Message("prefix")
    fun prefix(): Component

    @Message("info")
    fun sendVarpInfo(
        @Receiver receiver: Audience,
        @Placeholder("version") version: String,
        @Placeholder("platform") platform: String,
        @Placeholder("platform_brand") platformBrand: String,
        @Placeholder("platform_version") platformVersion: String,
    )

    @Message("reload.messages")
    fun sendReloadMessages(
        @Receiver receiver: Audience,
    )

    // region client link messages

    @Message("client.support_enabled")
    fun sendClientSupportEnabled(
        @Receiver receiver: Audience,
        @Placeholder("client") clientInformation: VarpClientInformation,
    )

    @Message("client.info.bridge_enabled")
    fun sendClientInfoBridgeEnabled(
        @Receiver receiver: Audience,
        @Placeholder("client") clientInformation: VarpClientInformation,
    )

    @Message("client.info.bridge_disabled")
    fun sendClientInfoBridgeDisabled(
        @Receiver receiver: Audience,
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

    // region teleportation log

    @Message("teleportation_history.send_to_previous_entry")
    fun sendTeleportationLogSendToPreviousEntry(
        @Receiver receiver: Audience,
        @Placeholder("entries") entries: Int,
    )

    @Message("teleportation_history.send_to_next_entry")
    fun sendTeleportationLogSendToNextEntry(
        @Receiver receiver: Audience,
        @Placeholder("entries") entries: Int,
    )

    // endregion

    // region repository messages

    @Message("repository.list.header")
    fun sendRepositoryListHeader(
        @Receiver receiver: Audience,
        @Placeholder("repositories") repositories: Int,
    )

    @Message("repository.list.entry_with_mounts")
    fun sendRepositoryListEntryWithMounts(
        @Receiver receiver: Audience,
        @Placeholder("repository_id") repositoryId: String,
        @Placeholder("repository_type") repositoryType: String,
        @Placeholder("repository_mounts") repositoryMounts: String,
    )

    @Message("repository.list.entry_without_mounts")
    fun sendRepositoryListEntryWithoutMounts(
        @Receiver receiver: Audience,
        @Placeholder("repository_id") repositoryId: String,
        @Placeholder("repository_type") repositoryType: String,
    )

    // endregion

    // region mount messages

    @Message("mount.list.header")
    fun sendMountListHeader(
        @Receiver receiver: Audience,
        @Placeholder("mounts") repositories: Int,
    )

    @Message("mount.list.entry")
    fun sendMountListEntry(
        @Receiver receiver: Audience,
        @Placeholder("mount_location") mountLocation: String,
        @Placeholder("mount_repository_id") mountRepositoryId: String,
        @Placeholder("mount_repository_path") mountRepositoryPath: String,
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
        @Placeholder("warp_path") warpPath: WarpPath,
        @Placeholder("warp") warpState: WarpState,
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

    @Message("warp.teleport.others_single")
    fun sendWarpTeleportOthersSingle(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
        @Placeholder("target") target: Component,
    )

    @Message("warp.teleport.others_multiple")
    fun sendWarpTeleportOthersMultiple(
        @Receiver receiver: Audience,
        @Placeholder("warp") warp: Warp,
        @Placeholder("target_count") targetCount: Int,
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
        @Placeholder("folder_path") folderPath: FolderPath,
        @Placeholder("folder") folderState: FolderState,
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

    @Message("error.missing_mount")
    fun sendErrorMissingMount(
        @Receiver receiver: Audience,
        @Placeholder("path") path: NodePath,
    )

    @Message("error.teleportation_log.no_previous_entry")
    fun sendErrorTeleportationLogNoPreviousEntry(
        @Receiver receiver: Audience,
    )

    @Message("error.teleportation_log.no_next_entry")
    fun sendErrorTeleportationLogNoNextEntry(
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

                    weightedPlaceholderResolver(Key::class.java, KeyPlaceholderResolver, 1)
                    weightedPlaceholderResolver(MinecraftLocation::class.java, MinecraftLocationPlaceholderResolver, 1)
                    weightedPlaceholderResolver(VarpServerPlayer::class.java, VarpServerPlayerPlaceholderResolver, 1)

                    weightedPlaceholderResolver(VarpClientInformation::class.java, VarpClientInformationPlaceholderResolver, 1)
                    weightedPlaceholderResolver(VarpServerInformation::class.java, VarpServerInformationPlaceholderResolver, 1)

                    weightedPlaceholderResolver(NodePath::class.java, NodePathPlaceholderResolver, 1)

                    weightedPlaceholderResolver(WarpState::class.java, WarpStatePlaceholderResolver, 1)
                    weightedPlaceholderResolver(FolderState::class.java, FolderStatePlaceholderResolver, 1)

                    weightedPlaceholderResolver(Warp::class.java, WarpPlaceholderResolver, 1)
                    weightedPlaceholderResolver(Folder::class.java, FolderPlaceholderResolver, 1)
                    weightedPlaceholderResolver(Root::class.java, RootPlaceholderResolver, 1)
                }
                .create(this::class.java.classLoader)
        }
    }
}
