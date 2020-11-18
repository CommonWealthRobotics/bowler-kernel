description = "An interface to a Git-based filesystem."

dependencies {
    api(project(":protoutil"))
    api(project(":auth"))
    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    api(project(":util"))
    api(group = "org.kohsuke", name = "github-api", version = Versions.githubAPI)
    api(group = "org.eclipse.jgit", name = "org.eclipse.jgit", version = Versions.jgit)

    testImplementation(project(":testUtil"))

    runtimeOnly(project(":logging"))
}
