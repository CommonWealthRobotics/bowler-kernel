description = "This module supports Vitamins."

dependencies {
    api(project(":bowler-kernel:gitfs"))
    api(project(":bowler-kernel:util"))

    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.5")
}
