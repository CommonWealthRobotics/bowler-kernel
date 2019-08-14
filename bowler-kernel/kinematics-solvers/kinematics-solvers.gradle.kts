import org.apache.commons.lang.SystemUtils

description = "Contains kinematics solvers."

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
    SystemUtils.IS_OS_MAC -> "osx"
    else -> throw IllegalStateException("Unknown OS: ${SystemUtils.OS_NAME}")
} + desktopArch()

fun DependencyHandler.bowlerKinematicsNative() =
    create(
        group = "com.neuronrobotics",
        name = "bowler-kinematics-native",
        version = property("bowler-kinematics-native.partial-version") as String + "-" +
            bowlerKinematicsNativeVersionSuffix()
    )

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(project(":bowler-kernel:kinematics"))

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

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))

    implementation(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )
    implementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )

    compileOnly(bowlerKinematicsNative())

    testImplementation(bowlerKinematicsNative())

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = property("mockito-kotlin.version") as String
    )
}
