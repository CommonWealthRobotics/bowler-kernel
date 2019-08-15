@file:Suppress("UnstableApiUsage")

val spotlessPluginVersion: String by settings
val ktlintPluginVersion: String by settings
val spotbugsPluginVersion: String by settings
val detektPluginVersion: String by settings
val bintrayPluginVersion: String by settings
val dokkaPluginVersion: String by settings
val testloggerPluginVersion: String by settings
val pitestPluginVersion: String by settings
val javafxpluginPluginVersion: String by settings

pluginManagement {
    plugins {
        id("com.diffplug.gradle.spotless") version spotlessPluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintPluginVersion
        id("com.github.spotbugs") version spotbugsPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektPluginVersion
        id("com.jfrog.bintray") version bintrayPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
        id("com.adarshr.test-logger") version testloggerPluginVersion
        id("info.solidsoft.pitest") version pitestPluginVersion
        id("org.openjfx.javafxplugin") version javafxpluginPluginVersion
    }
}

rootProject.name = "bowler-kernel"

include(":bowler-kernel")
include(":bowler-kernel:cad-core")
include(":bowler-kernel:cad-vitamins")
include(":bowler-kernel:config")
include(":bowler-kernel:gitfs")
include(":bowler-kernel:hardware")
include(":bowler-kernel:kinematics")
include(":bowler-kernel:kinematics-factories")
include(":bowler-kernel:kinematics-solvers")
include(":bowler-kernel:logging")
include(":bowler-kernel:old-stack-converter")
include(":bowler-kernel:scripting")
include(":bowler-kernel:util")
include(":bowler-kernel:vitamins")

/**
 * This configures the gradle build so we can use non-standard build file names.
 * Additionally, this project can support sub-projects who's build file is written in Kotlin.
 *
 * @param project The project to configure.
 */
fun configureGradleBuild(project: ProjectDescriptor) {
    val projectBuildFileBaseName = project.name
    val gradleBuild = File(project.projectDir, "$projectBuildFileBaseName.gradle")
    val kotlinBuild = File(project.projectDir, "$projectBuildFileBaseName.gradle.kts")
    assert(!(gradleBuild.exists() && kotlinBuild.exists())) {
        "Project ${project.name} can not have both a ${gradleBuild.name} and a ${kotlinBuild.name} file. " +
            "Rename one so that the other can serve as the base for the project's build"
    }
    project.buildFileName = when {
        gradleBuild.exists() -> gradleBuild.name
        kotlinBuild.exists() -> kotlinBuild.name
        else -> throw AssertionError(
            "Project `${project.name}` must have a either a file " +
                "containing ${gradleBuild.name} or ${kotlinBuild.name}"
        )
    }

    // Any nested children projects also get configured.
    project.children.forEach { configureGradleBuild(it) }
}

configureGradleBuild(rootProject)
