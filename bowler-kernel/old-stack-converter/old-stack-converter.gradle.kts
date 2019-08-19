import org.apache.commons.lang.SystemUtils

description = "Converts things from the old stack to the new stack."

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

dependencies {
    api(project(":bowler-kernel:kinematics"))
    api(project(":bowler-kernel:kinematics-solvers"))

    api(
        group = "com.neuronrobotics",
        name = "BowlerScriptingKernel",
        version = "0.45.1"
    )

    compileOnly(bowlerKinematicsNative())

    implementation(files("$rootDir/jbullet-2.72.2.4.jar"))

    testImplementation(bowlerKinematicsNative())

    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
}
