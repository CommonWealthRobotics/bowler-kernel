plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "bowler-kernel"

include(":logging")
include(":testUtil")
include(":util")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
