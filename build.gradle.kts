plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.github.lukesky19"
version = "1.0.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
    maven("https://repo.rosewooddev.io/repository/public/") {
        name = "RoseWood"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.2.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")

    // Hooks
    compileOnly("dev.rosewood:rosestacker:1.5.31")
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    compileOnly("com.ghostchu:quickshop-bukkit:6.2.0.8")
    compileOnly("com.ghostchu:quickshop-api:6.2.0.8")
    compileOnly("com.ghostchu:simplereloadlib:1.1.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
    archiveClassifier.set("")
    relocate("com.jeff_media.morepersistentdatatypes", "com.github.lukesky19.skyhoppers.libs.morepersistentdatatypes")
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}