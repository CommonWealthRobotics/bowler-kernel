description = "An interface to a Git-based filesystem."

dependencies {
    api(project(":protoutil"))
    api(project(":auth"))
    api(group = "io.arrow-kt", name = "arrow-fx-coroutines", version = Versions.arrow)

    implementation(project(":util"))
    implementation(group = "org.kohsuke", name = "github-api", version = Versions.githubAPI)
    implementation(group = "org.eclipse.jgit", name = "org.eclipse.jgit", version = Versions.jgit)

    testImplementation(project(":testUtil"))

    runtimeOnly(project(":logging"))
}
