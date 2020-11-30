import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version Versions.shadowPlugin
}

description = "The gRPC server."

dependencies {
    implementation(project(":scripthost"))
    implementation(project(":scripting"))
    implementation(project(":gitfs"))
    implementation(project(":di"))
    implementation(project(":util"))

    testImplementation(project(":testUtil"))
    testImplementation(testFixtures(project(":scripthost")))
}

tasks.withType<ShadowJar> { }
