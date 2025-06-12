plugins {
    java

    alias(libs.plugins.shadow)
    alias(libs.plugins.run.velocity)
    alias(libs.plugins.blossom)
    alias(libs.plugins.jetbrains.gradle)

    eclipse
    idea
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/") // Maven Central Snapshot Repository
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.micartey")
        }
    }
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
    implementation(libs.wordweaver)
    implementation(libs.crate.api)
    implementation(libs.crate.yaml)
    implementation(libs.colorparser) {
        exclude("net.kyori")
    }
    implementation(libs.github.api)
    compileOnly(libs.geyser)
    compileOnly(libs.floodgate)
    implementation(libs.bstats)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
        options.compilerArgs.addAll(arrayListOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))
    }

    javadoc {
        isFailOnError = false
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name()
        options.overview = "src/main/javadoc/overview.html"
        options.windowTitle = "${rootProject.name} Javadoc"
        options.tags("apiNote:a:API Note:", "implNote:a:Implementation Note:", "implSpec:a:Implementation Requirements:")
        options.addStringOption("Xdoclint:none", "-quiet")
        options.use()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        // Shadow classes
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.group}.vpack.lib.${targetPkg}")

        reloc("io.github.milkdrinkers.wordweaver", "wordweaver")
        reloc("io.github.milkdrinkers.crate", "crate")
        reloc("org.yaml.snakeyaml", "snakeyaml")
        reloc("io.github.milkdrinkers.colorparser", "colorparser")
        reloc("org.kohsuke.github", "github.api")
        reloc("org.bstats", "bstats")
        reloc("org.apache.commons", "commons")
        reloc("com.google.gson", "gson")
        reloc("com.google.errorprone", "errorprone")
        reloc("com.fasterxml.jackson", "jackson")

        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
        failFast = false
    }

    runVelocity {
        velocityVersion("3.4.0-SNAPSHOT")

        // IntelliJ IDEA debugger setup: https://docs.papermc.io/paper/dev/debugging#using-a-remote-debugger
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)

        // Automatically install dependencies
        downloadPlugins {
//            modrinth("carbon", "2.1.0-beta.21")
//            github("jpenilla", "MiniMOTD", "v2.0.13", "minimotd-bukkit-2.0.13.jar")
//            hangar("squaremap", "1.2.0")
//            url("https://download.luckperms.net/1515/bukkit/loader/LuckPerms-Bukkit-5.4.102.jar")
        }
    }
}

// Template generation
sourceSets {
    main {
        blossom {
            javaSources {
                property("name", project.name)
                property("description", project.description)
                property("version", project.version.toString())
            }
        }
    }
}