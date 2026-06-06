package net.voxelpi.varp.repository.filetree

import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.xml.XmlConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

enum class FileTreeStorageFormat(val extension: String, val provider: () -> AbstractConfigurationLoader.Builder<*, *>) {
    HOCON(".conf", HoconConfigurationLoader::builder),
    JSON(".json", GsonConfigurationLoader::builder),
    XML(".xml", XmlConfigurationLoader::builder),
    YAML(".yml", YamlConfigurationLoader::builder),
}
