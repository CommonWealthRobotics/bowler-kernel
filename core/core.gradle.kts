import org.apache.commons.lang3.SystemUtils

plugins {
    `java-library`
}

description = "The core module."

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(
            group = "org.apache.commons",
            name = "commons-lang3",
            version = property("commons-lang3.version") as String
        )
    }
}

fun desktopArch(): String {
    val arch = System.getProperty("os.arch")
    return if (arch == "amd64" || arch == "x86_64") "x86-64" else "x86"
}

fun bowlerKinematicsNativeVersionSuffix() = when {
    SystemUtils.IS_OS_WINDOWS -> "windows"
    SystemUtils.IS_OS_LINUX -> "linux"
    SystemUtils.IS_OS_MAC -> "macos"
    else -> throw IllegalStateException("Unknown OS: ${SystemUtils.OS_NAME}")
} + desktopArch()

fun DependencyHandler.bowlerKinematicsNative() =
    create(
        group = "com.neuronrobotics",
        name = "bowler-kinematics-native",
        version = property("bowler-kinematics-native.partial-version") as String + "-" +
            bowlerKinematicsNativeVersionSuffix()
    )

dependencies {
    api(
        group = "com.neuronrobotics",
        name = "java-bowler",
        version = property("java-bowler.version") as String
    )
    api(
        group = "org.apache.commons",
        name = "commons-math3",
        version = property("commons-math3.version") as String
    )
    api(
        group = "io.arrow-kt",
        name = "arrow-core",
        version = property("arrow.version") as String
    )
    api(
        group = "com.neuronrobotics",
        name = "bowler-kernel-kinematics",
        version = property("bowler-kernel.version") as String
    )

    compileOnly(bowlerKinematicsNative())

    implementation(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava.version") as String
    )

    implementation(
        group = "com.google.guava",
        name = "guava",
        version = property("guava.version") as String
    )
    implementation(
        group = "com.google.inject",
        name = "guice",
        version = property("guice.version") as String
    )
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = property("guice.version") as String
    )
    implementation(
        group = "org.jlleitschuh.guice",
        name = "kotlin-guiced-core",
        version = property("kotlin-guiced.version") as String
    )
    implementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )

    testImplementation(bowlerKinematicsNative())

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = property("mockito-kotlin.version") as String
    )
}
