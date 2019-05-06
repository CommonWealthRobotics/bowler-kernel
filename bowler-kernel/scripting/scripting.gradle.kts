plugins {
    `java-library`
}

description = "Support for some default scripting languages."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

repositories {
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
}

dependencies {
    api(project(":bowler-kernel:hardware"))
    api(project(":bowler-kernel:gitfs"))

    implementation(arrow("arrow-core-data"))
    implementation(arrow("arrow-core-extensions"))
    implementation(arrow("arrow-syntax"))
    implementation(arrow("arrow-typeclasses"))
    implementation(arrow("arrow-extras-data"))
    implementation(arrow("arrow-extras-extensions"))

    implementation(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )
    implementation(
        group = "com.google.inject",
        name = "guice",
        version = property("guice.version") as String
    )
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(
        group = "org.jlleitschuh.guice",
        name = "kotlin-guiced-core",
        version = property("kotlin-guiced-core.version") as String
    )
    implementation(group = "org.codehaus.groovy", name = "groovy", version = "2.5.4")
    implementation(group = "org.apache.ivy", name = "ivy", version = "2.4.0")
    implementation(group = "de.swirtz", name = "ktsRunner", version = "0.0.7")

    testImplementation(
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
