description = "Factories for the kinematics project."

dependencies {
    api(project(":bowler-kernel:kinematics"))
    api(project(":bowler-kernel:scripting"))

    testImplementation(
        group = "com.nhaarman.mockitokotlin2",
        name = "mockito-kotlin",
        version = property("mockito-kotlin.version") as String
    )
}
