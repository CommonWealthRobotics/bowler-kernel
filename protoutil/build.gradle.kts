description = "proto utilities."

dependencies {
    api(project(":proto"))
    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    implementation(project(":logging"))

    testImplementation(project(":testUtil"))
}
