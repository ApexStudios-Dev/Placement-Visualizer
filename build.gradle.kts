import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.75"
    id("apex-conventions.maven-publishing") version "0.1.75"
}

group = "dev.apexstudios"

apex.neoVersion("21.11.0-alpha.1.21.11-pre1.20251120.181430", "1.21.10", "2025.10.12")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()

repositories {
    apex.neoPrMaven(this, 2815)
}