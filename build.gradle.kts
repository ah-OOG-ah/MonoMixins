import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.renamer.gradle.RenameJar

plugins {
    id("java")
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[7.0.29,8.0)"
    id("net.minecraftforge.renamer") version "1.1.7"
    id("com.palantir.git-version") version "5.0.0"
    id("com.gradleup.shadow") version "9.6.1"
}

val minecraft_version: String = providers.gradleProperty("minecraft_version").get()
val forge_version: String = providers.gradleProperty("forge_version").get()

// TODO: Make this work with the configuration cache
@Suppress("UNCHECKED_CAST")
val gitVersion = project.extra["gitVersion"] as groovy.lang.Closure<String>

project.version = gitVersion()
project.group = "io.github.legacymoddingmc.unimixins"
project.base.archivesName = "+MonoMixins"

java.toolchain.languageVersion = JavaLanguageVersion.of(8)

minecraft {
    mappings("stable", "12-1.7.10")

    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("run"))
        }
        register("client")
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

val thinFML12 = sourceSets.create("thinFML12")
val shadowImplementation = configurations.create("shadowImplementation")
configurations.implementation {
    extendsFrom(shadowImplementation)
}

val mixinVersion = "0.17.3+mixin.0.8.7"
val mixinExtrasVersion = "0.5.4"

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${minecraft_version}-${forge_version}"))

    // TODO: make this cleaner than a copy/paste... maybe use artifact transforms?
    "thinFML12CompileOnly"("com.google.code.findbugs:jsr305:1.3.9")
    compileOnly(thinFML12.output)

    shadowImplementation("net.fabricmc:sponge-mixin:${mixinVersion}")
}

// Creates a task named 'renameJar'
renamer.classes(tasks.named<ShadowJar>("shadowJar")) {
    // You need to point to the mappings you wish to apply, typically this is the Mapped names to SRG for older versions.
    // ForgeGradle/Mavenizer generate these files for the dependencies you declare. So you can use the helper.
    // Or you can specify the file or dependency if you host them yourself.
    map.from(minecraft.dependency.toSrgFile)
    // This is publishable task so you can specify things such as the classifier
    archiveClassifier = ""
}

tasks.processResources {
    filesMatching("mcmod.info") {
        expand(
            "projectVersion" to project.version,
            "mixinVersion" to mixinVersion,
            "mixinExtrasVersion" to mixinExtrasVersion
        )
    }
}

tasks.jar {
    archiveClassifier = "dev-preshadow"

    from("LICENSE") {
        into("META-INF")
    }
    from("LICENSE.UNLICENSE") {
        into("META-INF")
    }

    manifest {
        attributes(mapOf(
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "FMLCorePlugin" to "io.github.legacymoddingmc.unimixins.all.AllCore",
            // This doesn't include MBL's json, and I don't know why.
            "MixinConfigs" to "mixins.gtnhmixins.json,mixingasm.mixin.json",
            "Premain-Class" to "org.spongepowered.tools.agent.MixinAgent",
            "Agent-Class" to "org.spongepowered.tools.agent.MixinAgent",
            "Can-Redefine-Classes" to true,
            "Can-Retransform-Classes" to true
        ))
    }
}

tasks.shadowJar {
    archiveClassifier = "dev"
    configurations.set(listOf(shadowImplementation))
}

publishing {
    repositories {
        maven { url = layout.projectDirectory.dir("repo").asFile.toURI() }
    }

    publications.register<MavenPublication>("mavenJava") {
        from(components["java"]) // Publish the normal jar
        artifact(tasks["renameShadowJar"]) // Publish the renamed jar in addition
    }
}
