description = "Benchmarks the DeviceServer."

dependencies {
    api(project(":device-server"))

    api(
        group = "org.apache.commons",
        name = "commons-math3",
        version = Versions.commonsMath3
    )

    testImplementation(project(":testUtil"))
}
