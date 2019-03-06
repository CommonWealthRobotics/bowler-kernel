plugins {
    `java-library`
}

description = "Support for some default scripting languages."

repositories {
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
}

dependencies {
    api(project(":bowler-kernel:hardware"))
    api(project(":bowler-kernel:gitfs"))

    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.5")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")
    implementation(group = "org.codehaus.groovy", name = "groovy", version = "2.5.4")
    implementation(group = "org.apache.ivy", name = "ivy", version = "2.4.0")
    implementation(group = "de.swirtz", name = "ktsRunner", version = "0.0.7")

    testImplementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0"
    )
}
