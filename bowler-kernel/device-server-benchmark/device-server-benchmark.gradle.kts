description = "Benchmarks the DeviceServer."

dependencies {
    api(project(":bowler-kernel:device-server"))

    implementation(
        group = "org.apache.commons",
        name = "commons-math3",
        version = property("commons-math3.version") as String
    )
}
