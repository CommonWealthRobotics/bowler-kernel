import Core_gradle.Verions.arrow_version
import Core_gradle.Verions.ktor_version

plugins {
    `java-library`
}

description = "The core module."

object Verions {
    const val arrow_version = "0.7.3"
    const val ktor_version = "1.0.1"
}

dependencies {
    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-syntax", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-typeclasses", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-data", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-instances-core", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-instances-data", version = arrow_version)
    kapt(group = "io.arrow-kt", name = "arrow-annotations-processor", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-free", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-mtl", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-effects", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-effects-rx2", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-effects-reactor", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-effects-kotlinx-coroutines", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-optics", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-generic", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-recursion", version = arrow_version)

    implementation(group = "com.google.guava", name = "guava", version = "25.0-jre")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    implementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")

    implementation(group = "com.beust", name = "klaxon", version = "4.+")
    implementation(group = "io.ktor", name = "ktor-client-core", version = ktor_version)
    implementation(group = "io.ktor", name = "ktor-client-core-jvm", version = ktor_version)
    implementation(group = "io.ktor", name = "ktor-client-apache", version = ktor_version)
    implementation(group = "io.ktor", name = "ktor-client-auth-basic", version = ktor_version)
    implementation(group = "io.ktor", name = "ktor-client-json-jvm", version = ktor_version)
    implementation(group = "io.ktor", name = "ktor-client-gson", version = ktor_version)

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0"
    )
    testImplementation(group = "io.ktor", name = "ktor-server-tests", version = ktor_version)
    testImplementation(group = "io.ktor", name = "ktor-client-mock", version = ktor_version)
}
