//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
//    id("com.github.johnrengelman.shadow") version Versions.shadowPlugin
    application
}

description = "The CLI used to manage the kernel server."

dependencies {
    api(project(":server"))
    api(group = "com.github.ajalt.clikt", name = "clikt", version = Versions.clikt)
}

//tasks.withType<ShadowJar> { }

application {
    mainClassName = "com.commonwealthrobotics.bowlerkernel.cli.Main"
}

tasks.withType<CreateStartScripts> {
    // Change the generated script name to bowler-kernel (defaults to the project name)
    applicationName = "bowler-kernel"
}
