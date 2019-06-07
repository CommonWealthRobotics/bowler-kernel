description = "The kinematics stack."

repositories {
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
}

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(project(":bowler-kernel:scripting"))
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
