description = "Implements the script host gRPC."

dependencies {
    implementation(project(":protoutil"))
    implementation(project(":scripting"))
    implementation(project(":gitfs"))

    testImplementation(project(":testUtil"))
}
