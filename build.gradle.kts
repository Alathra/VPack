version = "1.0.0"

plugins {
    java
    eclipse
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("xyz.jpenilla.run-velocity") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.alathra"

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("com.github.milkdrinkers:crate:1.2.1")
    implementation("com.github.micartey:webhookly:master-SNAPSHOT")
    implementation("org.kohsuke:github-api:1.302")
}

val targetJavaVersion = 17

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

// ✅ Directly configure the runVelocity task by name
tasks.named("runVelocity") {
    this.extensions.extraProperties["velocityVersion"] = "3.4.0-SNAPSHOT"
}

// ShadowJar relocation
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("AlathraResourcePack")
    archiveVersion.set("${project.version}")
    archiveClassifier.set("")
    relocate("com.github.milkdrinkers.Crate", "io.github.alathra.alathraResourcePack.libs.crate")
}

tasks.named<Jar>("jar") {
    enabled = false
}

// Template generation
val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates by tasks.registering(Copy::class) {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.named("main") {
    java.srcDir(generateTemplates.map { it.outputs })
}

eclipse {
    synchronizationTasks(generateTemplates)
}