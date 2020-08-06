plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "bowler-kernel"

include(":device-server")
include(":device-server-benchmark")
include(":gitfs")
include(":hardware")
include(":logging")
include(":testUtil")
include(":util")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
