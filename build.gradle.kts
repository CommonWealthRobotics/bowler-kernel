import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("com.diffplug.spotless") version Versions.spotlessPlugin
    id("com.adarshr.test-logger") version Versions.testLoggerPlugin
    jacoco
    kotlin("jvm") version Versions.kotlin
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlintPlugin
    `java-test-fixtures`
    id("org.jetbrains.dokka") version Versions.dokkaPlugin
}

val kotlinProjects = listOf(
    project(":auth"),
    project(":cli"),
    project(":device-server"),
    project(":device-server-benchmark"),
    project(":di"),
    project(":gitfs"),
    project(":hardware"),
    project(":logging"),
    project(":kernel-discovery"),
    project(":lifecycle"),
    project(":policyservice"),
    project(":protoutil"),
    project(":scripthost"),
    project(":scripting"),
    project(":server"),
    project(":testUtil"),
    project(":util")
)

val publishedProjects = kotlinProjects

allprojects {
    apply {
        plugin("com.diffplug.spotless")
        plugin("com.adarshr.test-logger")
    }

    group = "com.commonwealthrobotics"
    version = Versions.projectVersion

    repositories {
        mavenCentral()
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = Versions.jacocoTool
        }
    }

    tasks.withType<Test> {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    testlogger {
        theme = ThemeType.STANDARD_PARALLEL
        showStandardStreams = true
    }

    spotless {
        kotlinGradle {
            ktlint(Versions.ktlint)
            trimTrailingWhitespace()
        }
        freshmark {
            target("src/**/*.md")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
        format("extraneous") {
            target("src/**/*.fxml")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("jacoco")
        plugin("java-test-fixtures")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    tasks.test {
        useJUnitPlatform {
            filter {
                includeTestsMatching("*Test")
                includeTestsMatching("*Tests")
                includeTestsMatching("*Spec")
            }

            /*
            These tests just test performance and should not run in CI.
             */
            excludeTags("performance")

            /*
            These tests are too slow to run in CI.
             */
            excludeTags("slow")

            testLogging {
                events(
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.STARTED
                )
                displayGranularity = 0
                showExceptions = true
                showCauses = true
                showStackTraces = true
                exceptionFormat = TestExceptionFormat.FULL
                showStandardStreams = true
            }

            reports.junitXml.destination = file(rootProject.buildDir.toPath().resolve("test-results").resolve(project.name))
        }
    }

    tasks.withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    spotless {
        java {
            googleJavaFormat("1.8")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            licenseHeaderFile(rootProject.rootDir.toPath().resolve("config").resolve("spotless").resolve("license.txt"))
        }
    }
}

configure(kotlinProjects) {
    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("kotlin-kapt")
    }

    repositories {
        maven {
            setUrl("https://dl.bintray.com/arrow-kt/arrow-kt/")
        }
    }

    dependencies {
        implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = Versions.kotlin)
        implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = Versions.kotlin)
        implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Versions.kotlinCoroutines)
        implementation(group = "io.github.microutils", name = "kotlin-logging", version = Versions.kotlinLogging)

        testFixturesImplementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = Versions.kotlin)
        testFixturesImplementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = Versions.kotlin)
        testFixturesImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Versions.kotlinCoroutines)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }

    spotless {
        kotlin {
            ktlint(Versions.ktlint)
            licenseHeaderFile(rootProject.rootDir.toPath().resolve("config").resolve("spotless").resolve("license.txt"))
        }
    }

    // Always run ktlintFormat after spotlessApply
    tasks.named("spotlessApply").configure {
        finalizedBy(tasks.named("ktlintFormat"))
    }

    ktlint {
        version.set(Versions.ktlint)
//        enableExperimentalRules.set(true)

        additionalEditorconfigFile.set(file(rootProject.rootDir.toPath().resolve("config").resolve("ktlint").resolve(".editorconfig")))
        filter {
            exclude {
                it.file.path.contains("generated/")
            }
        }
    }
}

val jacocoRootReport by tasks.creating(JacocoReport::class) {
    group = "verification"
    val excludedProjects = listOf<Project>()
    val includedProjects = subprojects.filter { it !in excludedProjects }

    dependsOn(includedProjects.flatMap { it.tasks.withType(JacocoReport::class) } - this)

    val allSrcDirs = includedProjects.map { it.sourceSets.main.get().allSource.srcDirs }
    additionalSourceDirs.setFrom(allSrcDirs)
    sourceDirectories.setFrom(allSrcDirs)
    classDirectories.setFrom(includedProjects.map { it.sourceSets.main.get().output })
    executionData.setFrom(
        includedProjects.filter {
            File("${it.buildDir}/jacoco/test.exec").exists()
        }.flatMap { it.tasks.withType(JacocoReport::class).map { it.executionData } }
    )

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = Versions.gradleWrapper
}
