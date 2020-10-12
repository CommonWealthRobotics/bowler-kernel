import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("com.diffplug.spotless") version Versions.spotlessPlugin
    id("com.adarshr.test-logger") version Versions.testLoggerPlugin
    jacoco
    kotlin("jvm") version Versions.kotlin
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlintPlugin
    id("io.gitlab.arturbosch.detekt") version Versions.detektPlugin
    `java-test-fixtures`
    id("org.jetbrains.dokka") version Versions.dokkaPlugin
    id("com.jfrog.bintray") version Versions.bintrayPlugin
    `maven-publish`
}

val kotlinProjects = listOf(
    project(":auth"),
    project(":device-server"),
    project(":device-server-benchmark"),
    project(":di"),
    project(":gitfs"),
    project(":hardware"),
    project(":proto"),
    project(":protoutil"),
    project(":scripthost"),
    project(":scripting"),
    project(":server"),
    project(":testUtil"),
    project(":translator"),
    project(":util")
)

val publishedProjects = kotlinProjects

allprojects {
    apply {
        plugin("com.diffplug.spotless")
        plugin("com.adarshr.test-logger")
    }

    group = "com.commonwealthrobotics"
    version = Versions.bowlerKernel

    repositories {
        mavenCentral()
        jcenter()
        // Needed for bowler-script-kernel
        maven("https://dl.bintray.com/s1m0nw1/KtsRunner") {
            content {
                includeGroup("de.swirtz")
            }
        }
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
            targetExclude(project(":translator:bowler-script-kernel").projectDir.walkTopDown().toList())
        }
        freshmark {
            target("src/**/*.md")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            targetExclude(project(":translator:bowler-script-kernel").projectDir.walkTopDown().toList())
        }
        format("extraneous") {
            target("src/**/*.fxml")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            targetExclude(project(":translator:bowler-script-kernel").projectDir.walkTopDown().toList())
        }
    }
}

project(":translator:bowler-script-kernel") {
    apply {
        plugin("java-library")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }
}

subprojects {
    if (this in listOf(project(":translator:bowler-script-kernel"), project(":translator:bowler-script-kernel:java-bowler"), project(":translator:bowler-script-kernel:JCSG"))
    ) {
        return@subprojects
    }

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
            targetExclude(project(":translator:bowler-script-kernel").projectDir.walkTopDown().toList())
        }
    }
}

configure(kotlinProjects) {
    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
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
            // Generated proto sources
            targetExclude(project(":proto").buildDir.walkTopDown().toList())
        }
    }

    // Always run ktlintFormat after spotlessApply
    tasks.named("spotlessApply").configure {
        finalizedBy(tasks.named("ktlintFormat"))
    }

    ktlint {
        version.set(Versions.ktlint)
        enableExperimentalRules.set(true)
        additionalEditorconfigFile.set(file(rootProject.rootDir.toPath().resolve("config").resolve("ktlint").resolve(".editorconfig")))
        filter {
            exclude {
                it.file.path.contains("generated/")
            }
        }
    }

    detekt {
        input = files("src/main/kotlin", "src/test/kotlin")
        parallel = true
        config = files(rootProject.rootDir.toPath().resolve("config").resolve("detekt").resolve("config.yml"))
    }
}

configure(publishedProjects) {
    apply {
        plugin("com.jfrog.bintray")
        plugin("maven-publish")
    }

    val projectName = "bowler-kernel"

    task<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        archiveBaseName.set("$projectName-${this@configure.name.toLowerCase()}")
        from(sourceSets.main.get().allSource)
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        archiveBaseName.set("$projectName-${this@configure.name.toLowerCase()}")
        from(tasks.withType<DokkaTask>())
    }

    val publicationName = "publication-$projectName-${name.toLowerCase()}"

    publishing {
        publications {
            create<MavenPublication>(publicationName) {
                artifactId = "$projectName-${this@configure.name.toLowerCase()}"
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(dokkaJar)
            }
        }
    }

    bintray {
        val bintrayApiUser = properties["bintray.api.user"] ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = properties["bintray.api.key"] ?: System.getenv("BINTRAY_API_KEY")
        user = bintrayApiUser as String?
        key = bintrayApiKey as String?
        setPublications(publicationName)
        with(pkg) {
            repo = "maven-artifacts"
            name = projectName
            userOrg = "commonwealthrobotics"
            publish = true
            setLicenses("LGPL-3.0")
            vcsUrl = "https://github.com/CommonWealthRobotics/bowler-kernel.git"
            githubRepo = "https://github.com/CommonWealthRobotics/bowler-kernel"
            with(version) {
                name = Versions.bowlerKernel
                desc = "The heart of the Bowler stack."
            }
        }
    }
}

val jacocoRootReport by tasks.creating(JacocoReport::class) {
    group = "verification"
    val excludedProjects = listOf(
        project(":translator:bowler-script-kernel"),
        project(":translator:bowler-script-kernel:java-bowler"),
        project(":translator:bowler-script-kernel:JCSG")
    )
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
