import me.modmuss50.mpp.ReleaseType

plugins {
    id("java-library")
    id("net.neoforged.moddev")
    id("neoforge-mutex")
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin")
	id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.22"
}

version = "${sc.current.version}-${property("mod.version")}-${property("mod.suffix")}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val modId = property("mod.id") as String
val modVersion = property("mod.version") as String
val mcReleases = (sc.properties["mod.mc_releases"] as String)
	.split(",")
	.map { it.trim() }

repositories {
	mavenLocal()
	mavenCentral()
    maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
    maven("https://maven.latvian.dev/releases") { name = "Latvian" }
    maven("https://maven.blamejared.com/") { name = "Jared's maven" }

    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository { maven("https://www.cursemaven.com") { name = "CurseForge" } }
        filter { includeGroup("curse.maven") }
    }
	exclusiveContent { // ImGuiMC
		forRepository { maven("https://maven.ryanhcode.dev/releases") { name = "RyanHCode Maven" } }
		filter {
			includeGroup("foundry.imguimc")
		}
	}
}

neoForge {
    version = property("deps.neo_loader") as String
    accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    unitTest {
        enable()
        testedMod.set(mods.named(modId))
    }

    runs {
        configureEach {
            jvmArguments.add("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("terminal.ansi", "true")
        }
        register("client") {
            client()
            gameDirectory = file("../../runs/client")
        }
        register("server") {
            server()
            programArguments.add("--nogui")
            gameDirectory = file("../../runs/server")
        }
        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("../../runs/gameTestServer")
        }
        register("data") {
            clientData()
            serverData()
            programArguments.addAll(
                "--mod", modId,
                "--all",
                "--output", layout.buildDirectory.dir("generated/resources").get().asFile.absolutePath,
                "--existing", rootProject.file("src/main/resources").absolutePath,
            )
            gameDirectory = file("../../runs/data")
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    listOf(
        "com.vladsch.flexmark:flexmark-all:${property("lib.flexmark")}",
        "org.apache.groovy:groovy:${property("lib.groovy")}",
        "com.googlecode.soundlibs:jlayer:${property("lib.jlayer")}",
        "org.jflac:jflac-codec:${property("lib.jflac")}",
        "dev.vfyjxf:taffy:${property("lib.taffy")}",
    ).forEach { gav ->
        implementation(gav)
        api(gav)
        jarJar(gav)
    }

    // Third-party MC integrations are pinned only for 26.1 until 26.2 builds are published.
    if (sc.current.version == "26.1") {
		var mcVersion = property("mod.mc") as String
        runtimeOnly(fletchingTable.modrinth("jei", mcVersion, "neoforge"))
		compileOnly("foundry.imguimc:imguimc-neoforge-${sc.current.version}:${property("lib.imguimc")}")
		runtimeOnly("foundry.imguimc:imguimc-neoforge-${sc.current.version}:${property("lib.imguimc")}")
    } else {
		var mcVersion = property("mod.mc") as String
		runtimeOnly(fletchingTable.modrinth("jei", mcVersion, "neoforge"))
		compileOnly("foundry.imguimc:imguimc-neoforge-${mcVersion}:${property("lib.imguimc")}")
		runtimeOnly("foundry.imguimc:imguimc-neoforge-${mcVersion}:${property("lib.imguimc")}")
	}
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, name: String) {
            val value = sc.properties[name] as String
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("version", "mod.version")
            register("license", "mod.license")
            register("logo", "mod.logo")
			register("logo_key", "mod.logo_key")
            register("credits", "mod.credits")
            register("authors", "mod.authors")
            register("description", "mod.description")
            register("loader_version", "mod.loader_range")
            register("neo_range", "mod.neo_range")
            register("mc_compat", "mod.mc_compat")
			register("imguimc_compat", "lib.imguimc")
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    }

    withType<Javadoc> {
        (options as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            memberLevel = JavadocMemberLevel.PROTECTED
            links("https://docs.oracle.com/en/java/javase/25/docs/api/")
        }
        isFailOnError = false
        exclude("**/internal/**")
		source = sourceSets.main.get().allJava
    }

    named("assemble") {
        dependsOn("javadocJar")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to `build/libs/{version}/`."
        inputs.property("version", modVersion)
        dependsOn(named("jar"), named("sourcesJar"))
        from(named("jar"), named("sourcesJar"))
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Local"
            url = rootProject.file("repo").toURI()
        }
    }
}

publishMods {
	changelog = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
		.orElse(providers.environmentVariable("CHANGELOG"))
		.orElse("No changelog provided.")
	type = ReleaseType.ALPHA
	modLoaders.add("neoforge")
	version = "$modVersion-${sc.current.version}"
	displayName = "${property("mod.name")} $modVersion-${sc.current.version}"
	file = tasks.named("jar").flatMap { (it as Jar).archiveFile }
	additionalFiles.from(
		tasks.named("javadocJar").map { (it as Jar).archiveFile.get() },
		tasks.named("sourcesJar").map { (it as Jar).archiveFile.get() },
	)

	github {
		accessToken = providers.environmentVariable("GITHUB_TOKEN")
		repository = property("mod.github") as String
		commitish = property("mod.github_commitish") as String
		tagName = "v$modVersion-${sc.current.version}"
	}
	curseforge {
		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		projectId = "1605117"
		minecraftVersions.addAll(mcReleases)
		javaVersions.add(JavaVersion.VERSION_25)
		clientRequired = true
		serverRequired = true
		requires {
			slug = "imguimc"
		}
	}
	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "AaUmWHXd"
		minecraftVersions.addAll(mcReleases)
		requires {
			slug = "imguimc"
		}
	}
}

fletchingTable {
	lang.create("main") {
		patterns.add("assets/$modId/lang/**")
	}
	lang.all {
		sortKeys = true
		prettyPrint = true
		flatteningMode = "JOIN"
	}
}