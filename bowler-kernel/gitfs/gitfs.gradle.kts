plugins {
    `java-library`
    kotlin("kapt")
}

apply {
    from(rootProject.file("gradle/generated-kotlin-sources.gradle"))
}

description = "An interface to a Git-based filesystem."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(
        group = "org.kohsuke",
        name = "github-api",
        version = property("github-api.version") as String
    )
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )

    api(project(":bowler-kernel:config"))
    api(project(":bowler-kernel:logging"))

    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))
    api(arrow("arrow-effects-data"))
    api(arrow("arrow-effects-extensions"))
    api(arrow("arrow-effects-io-extensions"))

    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.2.0.201812061821-r"
    )

    implementation(
        group = "com.47deg",
        name = "helios-core",
        version = property("helios.version") as String
    )
    implementation(
        group = "com.47deg",
        name = "helios-parser",
        version = property("helios.version") as String
    )
    implementation(
        group = "com.47deg",
        name = "helios-optics",
        version = property("helios.version") as String
    )
    kapt(
        group = "com.47deg",
        name = "helios-meta",
        version = property("helios.version") as String
    )
    kapt(
        group = "com.47deg",
        name = "helios-dsl-meta",
        version = property("helios.version") as String
    )

    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
}
