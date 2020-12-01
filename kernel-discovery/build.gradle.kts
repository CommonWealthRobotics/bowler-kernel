description = "Implements the name server and client used for kernel discovery."

dependencies {
    api(group = "io.arrow-kt", name = "arrow-core-data", version = Versions.arrow)
    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)
    implementation(project(":util"))

    runtimeOnly(project(":logging"))

    testImplementation(project(":testUtil"))
}
