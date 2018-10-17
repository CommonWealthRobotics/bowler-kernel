plugins {
    `java-library`
}

description = "The kernel test module."

dependencies {
    testImplementation(project(":BowlerKernel:Core"))
}
