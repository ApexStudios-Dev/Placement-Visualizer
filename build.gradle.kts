import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.84"
    id("apex-conventions.maven-publishing") version "0.1.84"
}

group = "dev.apexstudios"

apex.neoVersion("21.11.0-beta", "1.21.10", "2025.10.12")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()