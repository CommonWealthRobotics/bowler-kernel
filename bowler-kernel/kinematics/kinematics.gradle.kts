description = "The kinematics stack."

repositories {
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
}

dependencies {
    api(group = "org.ejml", name = "ejml-all", version = "0.37.1")
    api(project(":bowler-kernel:scripting"))

    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.1")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")
    implementation(group = "com.beust", name = "klaxon", version = "4.+")

    testImplementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = "2.0.0"
    )
}
