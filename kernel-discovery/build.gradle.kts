description = "Implements the name server and client used for kernel discovery."

dependencies {
    implementation(project(":util"))

    runtimeOnly(project(":logging"))

    testImplementation(project(":testUtil"))
}
