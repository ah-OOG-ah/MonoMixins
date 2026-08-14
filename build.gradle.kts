plugins {
    id("java")
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[7.0.29,8.0)"
    id("net.minecraftforge.renamer") version "1.1.2"
}

val minecraft_version: String = providers.gradleProperty("minecraft_version").get()
val forge_version: String = providers.gradleProperty("forge_version").get()

version = "1.0"
group = "io.github.legacymoddingmc.unimixins"
base.archivesName = "MonoMixins"

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
dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${minecraft_version}-${forge_version}"))

    // TODO: make this cleaner than a copy/paste... maybe use artifact transforms?
    compileOnly(thinFML12.output)
}

// Creates a task named 'renameJar'
renamer.classes(tasks.named<Jar>("jar")) {
    // You need to point to the mappings you wish to apply, typically this is the Mapped names to SRG for older versions.
    // ForgeGradle/Mavenizer generate these files for the dependencies you declare. So you can use the helper.
    // Or you can specify the file or dependency if you host them yourself.
    map.from(minecraft.dependency.toSrgFile)
    // This is publishable task so you can specify things such as the classifier
    archiveClassifier = "srg"
}

// If you want to create another task, or customize the name you can specify it as the first argument
renamer.classes("renameJarToSrg", tasks.named<Jar>("jar")) {
    // This specifies the map via a dependency coordinate, such as 'net.minecraft:mappings_official:1.20.1-20230612.114412:map2srg@tsrg.gz'
    mappings(minecraft.dependency.toSrg.get())
}

tasks.jar {
    from("LICENSE") {
        into("META-INF")
    }
    from("LICENSE.UNLICENSE") {
        into("META-INF")
    }

    manifest {
        attributes(mapOf(
            "FMLCorePlugin" to "io.github.legacymoddingmc.unimixins.all.AllCore"
        ))
    }
}

publishing {
    repositories {
        maven { url = layout.projectDirectory.dir("repo").asFile.toURI() }
    }

    publications.register<MavenPublication>("mavenJava") {
        from(components["java"]) // Publish the normal jar
        artifact(tasks["renameJar"]) // Publish the renamed jar in addition
    }
}
