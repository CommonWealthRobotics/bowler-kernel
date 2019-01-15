plugins {
    `java-library`
}

description = "The kernel's logging tools."

dependencies {
    implementation(project(":bowler-kernel:config"))
}
