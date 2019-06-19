plugins {
    `java-library`
}

description = "The core module."

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

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = property("mockito-kotlin.version") as String
    )
}
