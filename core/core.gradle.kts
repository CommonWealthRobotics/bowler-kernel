description = "The core module."

plugins {
    `java-library`
}

dependencies {
    api(
        group = "com.neuronrobotics",
        name = "bowler-kernel-kinematics",
        version = property("bowler-kernel.version") as String
    )
    api(
        group = "com.neuronrobotics",
        name = "JavaCad",
        version = property("javacad.version") as String
    ) {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
}

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}
