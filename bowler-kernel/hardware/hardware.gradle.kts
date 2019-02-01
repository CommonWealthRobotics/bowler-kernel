import Hardware_gradle.Versions.arrow_version

plugins {
    `java-library`
}

description = "Controlled access to hardware resources."

object Versions {
    const val arrow_version = "0.8.1"
}

dependencies {
    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)
    api(group = "org.kohsuke", name = "github-api", version = "1.95")
    api(group = "com.neuronrobotics", name = "SimplePacketComsJava", version = "0.7.0")

    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.1")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")

    testImplementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0"
    )
}
