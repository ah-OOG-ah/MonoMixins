import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import java.util.jar.JarFile.MANIFEST_NAME
import java.util.jar.Attributes as JavaAttributes
import java.util.jar.Manifest as JavaManifest

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
    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/releases")
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

    // TODO: make this cleaner than a copy/paste... maybe use artifact transforms?
    "thinFML12CompileOnly"("com.google.code.findbugs:jsr305:1.3.9")
    compileOnly(thinFML12.output)

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

    compileOnly("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.1.1")
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

val expandableResources = setOf(
    "mcmod.info",
    "META-INF/rfb-plugin/monomixins.properties"
)
tasks.processResources {
    eachFile {
        if (path in expandableResources) {
            expand(
                "projectVersion" to project.version,
                "mixinVersion" to mixinVersion,
                "mixinExtrasVersion" to mixinExtrasVersion
            )
        }
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

/**
 * Appends the given entries to the first manifest found.
 */
class ManifestAppender(@get:Input entries: Map<String, Map<Any, Any>>): ResourceTransformer {
    private var manifest: JavaManifest? = null
    private val entries: Map<String, JavaAttributes> = entries.entries.map {
        val attrs = JavaAttributes()
        it.value.entries.forEach { attrs.putValue(it.key.toString(), it.value.toString()) }
        return@map Pair(it.key, attrs)
    }.toMap()

    override fun canTransformResource(element: FileTreeElement): Boolean {
        return MANIFEST_NAME.equals(element.path, ignoreCase = true)
    }

    override fun transform(context: TransformerContext) {
        if (manifest == null) {
            manifest = JavaManifest(context.inputStream)
        }
    }

    override fun hasTransformedResource(): Boolean = entries.isNotEmpty()

    override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
        os.putNextEntry(ZipEntry(MANIFEST_NAME))

        if (manifest == null) manifest = JavaManifest()
        manifest!!.entries.putAll(entries)
        manifest!!.write(os)

        os.closeEntry()
    }
}

tasks.shadowJar {
    archiveClassifier = "dev"
    configurations.set(listOf(shadowImplementation))

    // TODO: Figure out how to merge this from the ASM jar, instead of copying it manually
    transform(ManifestAppender(entries = mapOf(
        "org/spongepowered/asm/lib/" to mapOf("Implementation-Version" to asmVersion)
    )))
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
