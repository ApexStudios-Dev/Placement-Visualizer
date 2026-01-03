import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.88"
    id("apex-conventions.maven-publishing") version "0.1.88"
    id("apex-conventions.jspecify") version "0.1.88"
}

group = "dev.apexstudios"

apex.neoVersion("26.1.0.0-alpha.1+snapshot-1")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()