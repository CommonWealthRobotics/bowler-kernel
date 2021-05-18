description = "Implements the PolicyService RPC."

dependencies {
    api(project(":protoutil"))
    api(project(":util"))
    api(project(":di"))

    implementation(group = "io.arrow-kt", name = "arrow-core", version = Versions.arrow)

    testImplementation(project(":testUtil"))
}
