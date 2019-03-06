import Gitfs_gradle.Versions.arrow_version

plugins {
    `java-library`
}

description = "An interface to a Git-based filesystem."

object Versions {
    const val arrow_version = "0.8.1"
}

dependencies {
    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)
    api(group = "org.kohsuke", name = "github-api", version = "1.95")

    implementation(project(":bowler-kernel:config"))
    implementation(project(":bowler-kernel:logging"))
    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.1")
    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.2.0.201812061821-r"
    )
}
