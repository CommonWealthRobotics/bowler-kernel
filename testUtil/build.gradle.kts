description = "Test utilities for internal use."

dependencies {
    api(project(":di"))

    api(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.junit)
    api(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.junit)
    api(group = "org.junit.jupiter", name = "junit-jupiter-params", version = Versions.junit)

    api(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    api(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
    api(group = "io.kotest.extensions", name = "kotest-assertions-arrow", version = Versions.kotestAssertionsArrow)
    api(group = "io.kotest", name = "kotest-property-jvm", version = Versions.kotest)

    api(group = "io.mockk", name = "mockk", version = Versions.mockk)
    api(group = "io.arrow-kt", name = "arrow-core", version = Versions.arrow)
    api(group = "org.jacoco", name = "org.jacoco.agent", version = Versions.jacocoTool)
    api(group = "io.insert-koin", name = "koin-test", version = Versions.koin)
    api(group = "io.insert-koin", name = "koin-test-junit5", version = Versions.koin)
}
