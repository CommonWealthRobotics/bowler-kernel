import Core_gradle.Verions.arrow_version

plugins {
    `java-library`
}

description = "The core module."

object Verions {
    const val arrow_version = "0.7.3"
}

dependencies {
    api(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version = "0.32.4")
    api(group = "org.apache.commons", name = "commons-math3", version = "3.6.1")

    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-syntax", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-typeclasses", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-data", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-instances-core", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-instances-data", version = arrow_version)
    kapt(group = "io.arrow-kt", name = "arrow-annotations-processor", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-free", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-mtl", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-effects", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-effects-rx2", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-effects-reactor", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-effects-kotlinx-coroutines", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-optics", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-generic", version = arrow_version)
    implementation(group = "io.arrow-kt", name = "arrow-recursion", version = arrow_version)

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

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0-RC3"
    )
}
