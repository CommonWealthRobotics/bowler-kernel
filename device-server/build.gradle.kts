description = "A server that talks to microcontrollers."

dependencies {
    api(project(":util"))
    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    testImplementation(project(":testUtil"))

    runtimeOnly(project(":logging"))
}
