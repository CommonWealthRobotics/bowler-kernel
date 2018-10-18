plugins {
    `java-library`
}

description = "The kernel test module."

dependencies {
    testImplementation(project(":BowlerKernel:Core"))
    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0-RC3"
    )
}
