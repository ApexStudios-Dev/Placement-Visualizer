# Placement Visualizer

`Placement Visualizer` is a mod for Minecraft running on the [NeoForge modloader](https://neoforged.net/), which displays a preview of the block being placed.

### Obtaining Placement Visualizer

Placement Visualizer is a development library and is not intended for end-users to download directly. It can not be found on platforms like CurseForge or Modrinth.
To use it, you must add it as a dependency in your mod's `build.gradle` file.

The library is available through our [Maven repository](https://maven.apexstudios.dev/#/releases/dev/apexstudios/placementvisualizer).

<details>
<summary> Groovy DSL (build.gradle) </summary>

```groovy
repositories {
    maven { url "https://maven.apexstudios.dev/releases" }
}

dependencies {
    // Versions here must match
    implementation "dev.apexstudios:placementvisualizer:<version>"
    jarJar "dev.apexstudios:placementvisualizer:<version>"
}
```

</details>

<details>
<summary> Kotlin DSL (build.gradle.kts) </summary>

```groovy
repositories {
    maven("https://maven.apexstudios.dev/releases")
}

dependencies {
    // Versions here must match
    implementation("dev.apexstudios:placementvisualizer:<version>")
    jarJar("dev.apexstudios:placementvisualizer:<version>")
}
```

</details>
