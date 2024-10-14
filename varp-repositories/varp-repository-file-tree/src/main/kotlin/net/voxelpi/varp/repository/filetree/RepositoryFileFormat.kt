package net.voxelpi.varp.repository.filetree

import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.xml.XmlConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

enum class RepositoryFileFormat(val id: String, val extension: String, val provider: () -> AbstractConfigurationLoader.Builder<*, *>) {
    HOCON("hocon", ".conf", HoconConfigurationLoader::builder),
    JSON("json", ".json", GsonConfigurationLoader::builder),
    XML("xml", ".xml", XmlConfigurationLoader::builder),
    YAML("yaml", ".yml", YamlConfigurationLoader::builder),
    ;

    companion object {
        fun format(id: String): RepositoryFileFormat? {
            return RepositoryFileFormat.entries.find { it.id == id }
        }
    }
}
