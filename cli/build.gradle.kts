plugins {
    application
}

description = "The CLI used to manage the kernel server."

dependencies {
    api(project(":server"))

    implementation(project(":kernel-discovery"))

    implementation(group = "org.jline", name = "jline-terminal", version = Versions.jline)
    implementation(group = "org.jline", name = "jline-terminal-jansi", version = Versions.jline)
    implementation(group = "org.jline", name = "jline-terminal-jna", version = Versions.jline)
    implementation(group = "org.jline", name = "jline-reader", version = Versions.jline)
    implementation(group = "org.jline", name = "jline-style", version = Versions.jline)
    implementation(group = "org.jline", name = "jline-console", version = Versions.jline)
}

application {
    mainClassName = "com.commonwealthrobotics.bowlerkernel.cli.Main"
}

tasks.withType<CreateStartScripts> {
    // Change the generated script name to bowler-kernel (defaults to the project name)
    applicationName = "bowler-kernel"
}
