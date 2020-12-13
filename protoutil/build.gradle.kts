description = "proto utilities."

dependencies {
    api(group = "com.commonwealthrobotics", name = "bowler-proto-kotlin", version = Versions.bowlerProtoKotlin)
    api(group = "io.arrow-kt", name = "arrow-fx", version = Versions.arrow)

    testImplementation(project(":testUtil"))

    runtimeOnly(project(":logging"))
}
