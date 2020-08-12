description = "An interface to a Git-based filesystem."

dependencies {
    api(
        group = "org.kohsuke",
        name = "github-api",
        version = Versions.githubAPI
    )

    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    implementation(project(":logging"))
    implementation(project(":util"))
    implementation(project(":proto"))

    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = Versions.jgit
    )

    testImplementation(project(":testUtil"))
}
