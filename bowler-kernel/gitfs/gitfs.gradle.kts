plugins {
    `java-library`
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

    implementation(project(":bowler-kernel:config"))
    implementation(project(":bowler-kernel:logging"))
    implementation(arrow("arrow-core-data"))
    implementation(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )
    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.2.0.201812061821-r"
    )
}
