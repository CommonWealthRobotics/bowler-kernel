description = "A server that talks to microcontrollers."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )

    api(project(":bowler-kernel:util"))

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))
    api(arrow("arrow-effects-data"))
    api(arrow("arrow-effects-extensions"))
    api(arrow("arrow-effects-io-extensions"))

    implementation(project(":bowler-kernel:logging"))

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
    testImplementation(
        group = "io.mockk",
        name = "mockk",
        version = property("mockk.version") as String
    )
}
