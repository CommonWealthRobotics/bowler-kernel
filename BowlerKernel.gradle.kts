description = "The Bowler kernel."

spotless {
    java {
        licenseHeaderFile(
            "${rootProject.rootDir}/config/spotless/bowlerkernel.license",
            "(package|import)"
        )
    }
    kotlin {
        licenseHeaderFile(
            "${rootProject.rootDir}/config/spotless/bowlerkernel.license",
            "(package|import)"
        )
    }
}

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}
