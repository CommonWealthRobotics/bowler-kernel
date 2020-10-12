description = "proto utilities."

dependencies {
    api(project(":proto"))
    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    testImplementation(project(":testUtil"))

    runtimeOnly(project(":logging"))
}
