# Placement Visualizer

`Placement Visualizer` is a mod for Minecraft running on the [NeoForge modloader](https://neoforged.net/), which displays a preview of the block being placed.

This mod is available through our [Maven repository](https://maven.apexstudios.dev/releases).

<details>
<summary> Groovy DSL (build.gradle) </summary>

```groovy
repositories {
    maven { url "https://maven.apexstudios.dev/releases" }
}

dependencies {
    implementation "dev.apexstudios:placementvisualizer:<version>"
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
    implementation("dev.apexstudios:placementvisualizer:<version>")
}
```

</details>