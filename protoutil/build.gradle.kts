description = "proto utilities."

dependencies {
    api(project(":proto"))
    api(group = "io.arrow-kt", name = "arrow-core-data", version = Versions.arrow)

    testImplementation(project(":testUtil"))
}