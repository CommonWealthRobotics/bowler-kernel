description = "The gRPC server."

dependencies {
    implementation(project(":scripthost"))
    implementation(project(":scripting"))
    implementation(project(":gitfs"))
    implementation(project(":di"))

    testImplementation(project(":testUtil"))
}
