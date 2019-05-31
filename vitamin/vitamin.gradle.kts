description = "This module contains CAD generators for Vitamins."

plugins {
    `java-library`
}

dependencies {
    api(
        group = "com.neuronrobotics",
        name = "bowler-kernel-vitamins",
        version = property("bowler-kernel.version") as String
    )
    api(
        group = "com.neuronrobotics",
        name = "JavaCad",
        version = property("javacad.version") as String
    )

    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
}

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}
