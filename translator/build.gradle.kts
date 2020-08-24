dependencies {
    implementation(project(":translator:bowler-script-kernel"))
}

spotless {
    java {
        targetExclude("bowler-script-kernel/**")
    }
    kotlin {
        targetExclude("bowler-script-kernel/**")
    }
    kotlinGradle {
        targetExclude("bowler-script-kernel/**")
    }
    freshmark {
        targetExclude("bowler-script-kernel/**")
    }
    format("extraneous") {
        targetExclude("bowler-script-kernel/**")
    }
}
