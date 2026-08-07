import me.modmuss50.mpp.ReleaseType
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("java-library")
    id("net.neoforged.moddev")
    id("neoforge-mutex")
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin")
}

version = "${sc.current.version}+${property("mod.version")}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val modId = property("mod.id") as String
val modVersion = property("mod.version") as String
val mcReleases = sc.versions.map { it.version }

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
}

neoForge {
    version = property("deps.neo_loader") as String

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
    val imgui = property("lib.imgui") as String
    val commonmark = property("lib.commonmark") as String

    listOf(
        "org.commonmark:commonmark:$commonmark",
        "org.commonmark:commonmark-ext-gfm-tables:$commonmark",
        "org.commonmark:commonmark-ext-gfm-strikethrough:$commonmark",
        "org.commonmark:commonmark-ext-autolink:$commonmark",
        "org.apache.groovy:groovy:${property("lib.groovy")}",
        "com.googlecode.soundlibs:jlayer:${property("lib.jlayer")}",
        "org.jflac:jflac-codec:${property("lib.jflac")}",
        "dev.latvian.mods:renderdoc-support:${property("lib.renderdoc")}",
        "dev.vfyjxf:taffy:${property("lib.taffy")}",
        "io.github.spair:imgui-java-binding:$imgui",
        "io.github.spair:imgui-java-natives-windows:$imgui",
        "io.github.spair:imgui-java-natives-linux:$imgui",
        "io.github.spair:imgui-java-natives-macos:$imgui",
    ).forEach { gav ->
        implementation(gav)
        api(gav)
        jarJar(gav)
    }

    implementation("io.github.spair:imgui-java-lwjgl3:$imgui") { exclude(group = "org.lwjgl") }
    api("io.github.spair:imgui-java-lwjgl3:$imgui") { exclude(group = "org.lwjgl") }
    jarJar("io.github.spair:imgui-java-lwjgl3:$imgui") { exclude(group = "org.lwjgl") }

    // Third-party MC integrations are pinned only for 26.1 until 26.2 builds are published.
    if (sc.current.version == "26.1") {
        compileOnly("maven.modrinth:AANobbMI:${property("lib.sodium")}")
        runtimeOnly("maven.modrinth:AANobbMI:${property("lib.sodium")}")
        compileOnly("maven.modrinth:YL57xq9U:${property("lib.iris")}")
        runtimeOnly("maven.modrinth:YL57xq9U:${property("lib.iris")}")

        val jei = property("deps.jei") as String
        compileOnly("mezz.jei:jei-${property("mod.mc")}-neoforge-api:$jei")
        runtimeOnly("mezz.jei:jei-${property("mod.mc")}-neoforge:$jei")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${property("lib.junit")}")
    testImplementation("net.neoforged:testframework:${property("deps.neo_loader")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${property("lib.junit_platform")}")
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
            register("credits", "mod.credits")
            register("authors", "mod.authors")
            register("description", "mod.description")
            register("loader_version", "mod.loader_range")
            register("neo_range", "mod.neo_range")
            register("mc_compat", "mod.mc_compat")
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
    changelog = providers.environmentVariable("CHANGELOG").orElse("No changelog provided.")
    type = ReleaseType.ALPHA
    modLoaders.add("neoforge")
    version = modVersion
    displayName = "${property("mod.name")} $modVersion"
    file = tasks.named("jar").flatMap { (it as Jar).archiveFile }
    additionalFiles.from(
        tasks.named("javadocJar").map { (it as Jar).archiveFile.get() },
        tasks.named("sourcesJar").map { (it as Jar).archiveFile.get() },
    )

    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = property("mod.github") as String
        commitish = property("mod.github_commitish") as String
        tagName = "v$modVersion"
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1605117"
        minecraftVersions.addAll(mcReleases)
        javaVersions.add(JavaVersion.VERSION_25)
        clientRequired = true
        serverRequired = true
    }
    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "AaUmWHXd"
        minecraftVersions.addAll(mcReleases)
    }
}