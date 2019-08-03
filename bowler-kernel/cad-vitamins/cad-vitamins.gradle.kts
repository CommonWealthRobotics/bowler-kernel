description = "CAD generators for Vitamins."

plugins {
    `java-library`
}

dependencies {
    api(project(":bowler-kernel:vitamins"))

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
