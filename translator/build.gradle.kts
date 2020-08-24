dependencies {
    implementation(project(":translator:bowler-script-kernel"))
}

spotless {
    java {
        targetExclude("bowler-script-kernel/**")
    }
}
