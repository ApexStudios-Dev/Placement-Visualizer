plugins {
    id("apex-conventions.neoforge") version "0.1.92-beta-pr-12"
    id("apex-conventions.maven-publishing") version "0.1.92-beta-pr-12"
}

group = "dev.apexstudios"

sourceSets {
    main {
        resources.srcDir(file("src/data/generated"))
    }

    create("data") {
        java.setSrcDirs(files("src/data"))
        resources.setSrcDirs(files())

        compileClasspath += main.get().output
        runtimeClasspath += main.get().output

        tasks.findByName("compileJava")?.finalizedBy(compileJavaTaskName)
        tasks.findByName("classes")?.finalizedBy(classesTaskName)
        tasks.findByName("processResources")?.finalizedBy(processResourcesTaskName)
    }
}

neoForge {
    enable {
        version = "26.1.0.0-alpha.1+snapshot-1"
        isDisableRecompilation = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)
        enabledSourceSets = setOf(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME], sourceSets["data"])
    }

    val dataMod = mods.create("data") {
        sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        sourceSet(sourceSets["data"])
    }

    runs.create("data") {
        if(versionCapabilities.splitDataRuns()) {
            clientData()
        } else {
            data()
        }

        sourceSet.set(sourceSets["data"])
        loadedMods.set(listOf(dataMod))
        ideName.set("Data")

        programArguments.addAll(
            "--mod", "placementvisualizer",
            "--all",
            "--output", file("src/data/generated").absolutePath,
            "--existing", file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources").absolutePath
        )
    }
}

repositories {
    maven("https://prmaven.neoforged.net/NeoForge/pr2879")
}