import Core_gradle.Versions.arrow_version

plugins {
    `java-library`
}

description = "The core module."

object Versions {
    const val arrow_version = "0.7.3"
}

dependencies {
    api(group = "com.neuronrobotics", name = "java-bowler", version = "3.26.2")
    api(group = "org.apache.commons", name = "commons-math3", version = "3.6.1")
    api(group = "org.ejml", name = "ejml-all", version = "0.36")

    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)

    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.5")

    implementation(group = "com.google.guava", name = "guava", version = "25.0-jre")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")
    implementation(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    implementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0"
    )

    testImplementation(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version = "0.32.4") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
        exclude(group = "com.google.guava")
        exclude(group = "org.mockito")
    }
}
