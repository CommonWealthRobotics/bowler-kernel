plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "bowler-kernel"

include(":device-server")
include(":device-server-benchmark")
include(":gitfs")
include(":hardware")
include(":logging")
include(":proto")
include(":protoutil")
include(":scripthost")
include(":scripting")
include(":testUtil")
includeBuild("translator/bowler-script-kernel")
include(":translator")
include(":util")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
