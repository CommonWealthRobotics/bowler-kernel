description = "proto utilities."

dependencies {
    api(project(":proto"))

    implementation(group = "io.arrow-kt", name = "arrow-core-data", version = Versions.arrow)
    implementation(project(":logging"))

    testImplementation(project(":testUtil"))
}
