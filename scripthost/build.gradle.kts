description = "Implements the script host gRPC."

dependencies {
    api(project(":gitfs"))
    api(project(":protoutil"))
    api(project(":util"))
    api(project(":di"))

    implementation(project(":scripting"))
    implementation(group = "io.arrow-kt", name = "arrow-core-data", version = Versions.arrow)
    implementation(group = "io.arrow-kt", name = "arrow-core", version = Versions.arrow)

    testImplementation(project(":testUtil"))

    testFixturesApi(project(":scripting"))
}
