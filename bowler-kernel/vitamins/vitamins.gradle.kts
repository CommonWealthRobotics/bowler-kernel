description = "This module supports Vitamins."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(project(":bowler-kernel:gitfs"))
    api(project(":bowler-kernel:util"))
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )
    api(
        group = "org.octogonapus",
        name = "kt-guava-klaxon",
        version = property("kt-guava-klaxon.version") as String
    )
    api(
        group = "org.octogonapus",
        name = "kt-units-annotation",
        version = property("kt-units.version") as String
    )
    api(
        group = "org.octogonapus",
        name = "kt-units-quantities",
        version = property("kt-units.version") as String
    )
    api(
        group = "com.neuronrobotics",
        name = "JavaCad",
        version = property("javacad.version") as String
    ) {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))

    implementation(
        group = "com.beust",
        name = "klaxon",
        version = property("klaxon.version") as String
    )

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
