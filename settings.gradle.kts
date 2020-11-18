pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "bowler-kernel"

include(":auth")
include(":cli")
include(":device-server")
include(":device-server-benchmark")
include(":di")
include(":gitfs")
include(":hardware")
include(":logging")
include(":proto")
include(":protoutil")
include(":scripthost")
include(":scripting")
include(":server")
include(":testUtil")
include(":util")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
