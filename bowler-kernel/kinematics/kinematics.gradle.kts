description = "The kinematics stack."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(project(":bowler-kernel:gitfs"))
    api(project(":bowler-kernel:util"))

    api(
        group = "org.apache.commons",
        name = "commons-math3",
        version = property("commons-math3.version") as String
    )
    api(
        group = "gov.nist.math",
        name = "jama",
        version = property("jama.version") as String
    )
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))

    api(
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
    testImplementation(
        group = "io.mockk",
        name = "mockk",
        version = property("mockk.version") as String
    )
}
