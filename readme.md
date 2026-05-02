# Varp

![GitHub CI Status](https://img.shields.io/github/actions/workflow/status/voxelpi/varp/build-publish-commit.yml?branch=main&label=CI&style=for-the-badge)
[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/net.voxelpi.varp/varp-api?server=https%3A%2F%2Frepo.voxelpi.net&nexusVersion=3&style=for-the-badge&label=stable&color=blue)](https://repo.voxelpi.net/#browse/search=keyword%3Dvarp)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/net.voxelpi.varp/varp-api?server=https%3A%2F%2Frepo.voxelpi.net&nexusVersion=3&style=for-the-badge&label=dev)](https://repo.voxelpi.net/#browse/search=keyword%3Dvarp)

A flexible and modular warp management system for Minecraft that combines powerful organization with a smooth user experience.

Designed for both server administrators and players, it brings a filesystem-like structure to warps, 
supports multiple storage backends, and offers a rich client interface for intuitive interaction.

Key features:

- **Hierarchical warp tree** - Organize warps in folders and subfolders, just like a filesystem.
- **Pluggable storage backends** - Use SQL, JSON, or other storage solutions interchangeably.
- **Mount-like storage system** - Mix and integrate multiple storage types seamlessly within the same tree.
- **Rich client GUI** - Provide players with a modern, intuitive interface for browsing and managing warps.

For more information about the minecraft mod see [varp-mod](./varp-mod).

## Developers

### Getting started

build.gradle.kts

```kotlin
repositories {
    maven {
        url = uri("https://repo.voxelpi.net/repository/maven-public/")
    }
}

dependencies {
    // Core library, required to use the varp api.
    implementation("net.voxelpi.varp:varp-core:<version>")

    // Environment, provides ability to load and manage multiple repositories.
    implementation("net.voxelpi.varp:varp-environment:<version>") 
    
    // Varp data serializers, provides ways to serialize varp data to common formats.
    implementation("net.voxelpi.varp:varp-serializer-configurate:<version>")
    implementation("net.voxelpi.varp:varp-serializer-gson:<version>")
    
    // More repository types, provides ways to store varp data.
    implementation("net.voxelpi.varp:varp-repository-file-tree:<version>")
    implementation("net.voxelpi.varp:varp-repository-mysql:<version>")
}
```
You can get the latest version number from the [GitHub page](https://github.com/VoxelPi/Varp)
