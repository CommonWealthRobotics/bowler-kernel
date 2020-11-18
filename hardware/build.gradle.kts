apply {
    plugin("kotlin-kapt")
}

description = "Controlled access to hardware resources."

dependencies {
    api(
        group = "org.kohsuke",
        name = "github-api",
        version = Versions.githubAPI
    )

    api(project(":util"))
    api(project(":device-server"))

    api(group = "io.arrow-kt", name = "arrow-core", version = Versions.arrow)
    api(group = "io.arrow-kt", name = "arrow-syntax", version = Versions.arrow)
    api(group = "io.arrow-kt", name = "arrow-optics", version = Versions.arrow)
    kapt("io.arrow-kt:arrow-meta:${Versions.arrow}")

    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = Versions.ktGuavaCore
    )

    testImplementation(project(":testUtil"))

    runtimeOnly(project(":logging"))
}
