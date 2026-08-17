import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.register

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

// Mixin 0.7.x shaded ASM, and UniMixins continued doing this up to ASM 9.9.1.
// As such, we make a sourceset to contain this shaded ASM (but marked as deprecated)
val mixin7ASM = sourceSets.create("mixin7ASM")
val mixin7ASMImplementation = configurations.named("mixin7ASMImplementation")
// Do not upgrade this!
val shadedASMVersion = "9.9.1"
val shadedASMPackage = "org.spongepowered.asm.lib"
val shadedASMLocation = shadedASMPackage.replace(".", "/") + "/"

val shadowMixin7ASM = tasks.register<ShadowJar>("shadowMixin7ASM") {
    description = "Create relocated ASM from UniMixins, and mark it as deprecated"
    configurations = setOf(project.configurations.named("mixin7ASMCompileClasspath").get())
    relocate("org.objectweb.asm", shadedASMPackage)
    archiveClassifier = "mixin7ASM"

    exclude("module-info.class")

    manifest {
        attributes(
            mapOf("Implementation-Version" to shadedASMVersion),
            shadedASMLocation
        )
    }
}

val thinFML12 = sourceSets.create("thinFML12")
val shadowImplementation = configurations.create("shadowImplementation")
configurations.implementation {
    extendsFrom(shadowImplementation)
}

val mixinVersion = "0.17.3+mixin.0.8.7"
val mixinExtrasVersion = "0.5.4"
val asmVersion = "9.10.1"

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${minecraft_version}-${forge_version}"))

    mixin7ASMImplementation("org.ow2.asm:asm:${shadedASMVersion}")
    mixin7ASMImplementation("org.ow2.asm:asm-analysis:${shadedASMVersion}")
    mixin7ASMImplementation("org.ow2.asm:asm-commons:${shadedASMVersion}")
    mixin7ASMImplementation("org.ow2.asm:asm-tree:${shadedASMVersion}")
    mixin7ASMImplementation("org.ow2.asm:asm-util:${shadedASMVersion}")

    // TODO: make this cleaner than a copy/paste... maybe use artifact transforms?
    "thinFML12CompileOnly"("com.google.code.findbugs:jsr305:1.3.9")
    compileOnly(thinFML12.output)

    shadowImplementation(shadowMixin7ASM.get().outputs.files)
    shadowImplementation("net.fabricmc:sponge-mixin:${mixinVersion}") {
        exclude("org.ow2.asm")
    }
    shadowImplementation("io.github.llamalad7:mixinextras-common:${mixinExtrasVersion}") {
        exclude("org.ow2.asm")
    }

    implementation("org.ow2.asm:asm:${asmVersion}")
    implementation("org.ow2.asm:asm-analysis:${asmVersion}")
    implementation("org.ow2.asm:asm-commons:${asmVersion}")
    implementation("org.ow2.asm:asm-tree:${asmVersion}")
    implementation("org.ow2.asm:asm-util:${asmVersion}")
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

    // Prefer shaded classes emulating Unimixins 0.3.1/Mixin 0.7
    filesMatching("${shadedASMLocation.replace(".", "/")}/*") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
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
