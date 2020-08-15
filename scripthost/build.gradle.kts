description = "Implements the script host gRPC."

dependencies {
    api(project(":protoutil"))

    implementation(project(":scripting"))
    implementation(project(":gitfs"))
    implementation(project(":di"))
    implementation(group = "io.arrow-kt", name = "arrow-core-data", version = Versions.arrow)
    implementation(group = "io.arrow-kt", name = "arrow-core", version = Versions.arrow)

    testImplementation(project(":testUtil"))
}
