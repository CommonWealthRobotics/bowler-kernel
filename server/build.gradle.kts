description = "The gRPC server."

dependencies {
    implementation(project(":scripthost"))
    implementation(project(":scripting"))
    implementation(project(":gitfs"))
    implementation(project(":di"))
    implementation(project(":util"))

    testImplementation(project(":testUtil"))
    testImplementation(testFixtures(project(":scripthost")))
}
