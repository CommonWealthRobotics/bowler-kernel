description = "Benchmarks the DeviceServer."

dependencies {
    implementation(project(":device-server"))

    implementation(
        group = "org.apache.commons",
        name = "commons-math3",
        version = Versions.commonsMath3
    )

    testImplementation(project(":testUtil"))
}
