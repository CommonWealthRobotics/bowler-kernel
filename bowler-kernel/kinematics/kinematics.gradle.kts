description = "The kinematics stack."

dependencies {
    // For emjl and some utils
    api(
        group = "com.neuronrobotics",
        name = "kinematicschef-core",
        version = "0.0.15"
    ) {
        exclude(group = "com.neuronrobotics")
    }
    api(project(":bowler-kernel:hardware"))

    implementation(project(":bowler-kernel:util"))
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
