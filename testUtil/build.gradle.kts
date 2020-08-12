description = "Test utilities for internal use."

dependencies {
    api(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.junit)
    api(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.junit)
    api(group = "org.junit.jupiter", name = "junit-jupiter-params", version = Versions.junit)

    api(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    api(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
    api(group = "io.kotest", name = "kotest-assertions-arrow", version = Versions.kotest)
    api(group = "io.kotest", name = "kotest-property-jvm", version = Versions.kotest)

    api(group = "io.mockk", name = "mockk", version = Versions.mockk)

    api(group = "io.arrow-kt", name = "arrow-core-data", version = Versions.arrow)

    api(group = "org.jacoco", name = "org.jacoco.agent", version = Versions.jacocoTool)

    implementation(project(":proto"))
}
