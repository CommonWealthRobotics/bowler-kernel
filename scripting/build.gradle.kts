description = "Support for loading and running scripts."

dependencies {
    implementation(project(":gitfs"))
    implementation(project(":hardware"))
    implementation(project(":proto"))

    implementation(group = "org.codehaus.groovy", name = "groovy", version = Versions.groovy)
    implementation(group = "org.apache.ivy", name = "ivy", version = Versions.ivy)
    implementation(group = "io.arrow-kt", name = "arrow-core", version = Versions.arrow)
    implementation(group = "io.arrow-kt", name = "arrow-syntax", version = Versions.arrow)
    implementation(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    testImplementation(project(":testUtil"))
}
