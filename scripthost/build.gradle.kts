description = "Implements the script host gRPC."

dependencies {
    implementation(project(":proto"))
    implementation(project(":scripting"))
    implementation(project(":gitfs"))

    testImplementation(project(":testUtil"))
}
