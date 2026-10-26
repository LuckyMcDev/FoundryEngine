pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.develocity") version "4.3.2"
}

stonecutter {
    create(rootProject) {
		// This needs to be updated to 26.1 and 27.1 as i will be supporting first version of each year release and not patches.
		// See KJS LTS version support.
        versions("26.1", "26.2")
        vcsVersion = "26.1"
    }
}

rootProject.name = "FoundryEngine"

include("TestBundle")
include("docs")