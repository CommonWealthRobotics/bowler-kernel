import Core_gradle.Versions.kernel_version

description = "The core module."

plugins {
    `java-library`
}

object Versions {
    const val kernel_version = "0.0.20"
}

dependencies {
    api(group = "com.neuronrobotics", name = "bowler-kernel-kinematics", version = kernel_version)
    api(group = "com.neuronrobotics", name = "JavaCad", version = "0.18.1")

    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.5")

    testImplementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
}

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}
