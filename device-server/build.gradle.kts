description = "A server that talks to microcontrollers."

dependencies {
    api(project(":util"))

    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    implementation(project(":logging"))
    implementation(project(":proto"))

    testImplementation(project(":testUtil"))
}
