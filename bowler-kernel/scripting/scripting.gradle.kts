plugins {
    `java-library`
}

description = "Support for some default scripting languages."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(project(":bowler-kernel:hardware"))
    api(project(":bowler-kernel:gitfs"))
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )
    api(
        group = "com.google.inject",
        name = "guice",
        version = property("guice.version") as String
    )
    api(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))

    implementation(
        group = "org.jlleitschuh.guice",
        name = "kotlin-guiced-core",
        version = property("kotlin-guiced-core.version") as String
    )
    implementation(group = "org.codehaus.groovy", name = "groovy", version = "2.5.4")
    implementation(group = "org.apache.ivy", name = "ivy", version = "2.4.0")
    implementation(group = "de.swirtz", name = "ktsRunner", version = "0.0.7") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }

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
