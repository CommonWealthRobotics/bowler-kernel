import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.spotbugs.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths

plugins {
    jacoco
    pmd
    id("com.diffplug.gradle.spotless") version "3.22.0"
    id("org.jlleitschuh.gradle.ktlint") version "7.3.0"
    id("com.github.spotbugs") version "1.7.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.3"
    `java-library`
    id("org.jetbrains.dokka") version "0.9.18"
    id("com.adarshr.test-logger") version "1.6.0"
    checkstyle
}

val bowlerKernelVersion = "0.1.7"
val ktlintVersion = "0.29.0"
val junitJupiterVersion = "5.4.0"
val jacocoToolVersion = "0.8.3"
val checkstyleToolVersion = "8.1"
val spotbugsToolVersion = "4.0.0-beta1"
val pmdToolVersion = "6.3.0"
val detektToolVersion = "1.0.0-RC12"

val spotlessLicenseHeaderDelimiter = "(@|package|import)"

val bowlerKernelProject = project(":bowler-kernel")
val bowlerKernelSettingsProject = project(":bowler-kernel:config")
val bowlerKernelGitFSProject = project(":bowler-kernel:gitfs")
val bowlerKernelHardwareProject = project(":bowler-kernel:hardware")
val bowlerKernelKinematicsProject = project(":bowler-kernel:kinematics")
val bowlerKernelLoggingProject = project(":bowler-kernel:logging")
val bowlerKernelScriptingProject = project(":bowler-kernel:scripting")
val bowlerKernelUtilProject = project(":bowler-kernel:util")
val bowlerKernelVitaminsProject = project(":bowler-kernel:vitamins")

val kotlinProjects = setOf(
    bowlerKernelProject,
    bowlerKernelSettingsProject,
    bowlerKernelGitFSProject,
    bowlerKernelHardwareProject,
    bowlerKernelKinematicsProject,
    bowlerKernelLoggingProject,
    bowlerKernelScriptingProject,
    bowlerKernelUtilProject,
    bowlerKernelVitaminsProject
)

val javaProjects = setOf<Project>() + kotlinProjects

val publishedProjects = setOf(
    bowlerKernelSettingsProject,
    bowlerKernelGitFSProject,
    bowlerKernelHardwareProject,
    bowlerKernelKinematicsProject,
    bowlerKernelLoggingProject,
    bowlerKernelScriptingProject,
    bowlerKernelUtilProject,
    bowlerKernelVitaminsProject
)

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/staging/")
    }

    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlin.version") as String))
    }
}

allprojects {
    version = bowlerKernelVersion
    group = "com.neuronrobotics"

    apply {
        plugin("com.diffplug.gradle.spotless")
        plugin("com.adarshr.test-logger")
    }

    repositories {
        jcenter()
        mavenCentral()
        maven("https://dl.bintray.com/octogonapus/maven-artifacts")
        maven("https://oss.sonatype.org/content/repositories/staging/")
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = jacocoToolVersion
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
    }

    spotless {
        /*
         * We use spotless to lint the Gradle Kotlin DSL files that make up the build.
         * These checks are dependencies of the `check` task.
         */
        kotlinGradle {
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
        }
        freshmark {
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

fun DependencyHandler.junitJupiter(name: String) =
    create(group = "org.junit.jupiter", name = name, version = junitJupiterVersion)

configure(javaProjects) {
    apply {
        plugin("java")
        plugin("jacoco")
        plugin("checkstyle")
        plugin("com.github.spotbugs")
        plugin("pmd")
    }

    dependencies {
        testCompile(junitJupiter("junit-jupiter"))

        testRuntime(
            group = "org.junit.platform",
            name = "junit-platform-launcher",
            version = "1.0.0"
        )
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    tasks.withType<Test> {
        @Suppress("UnstableApiUsage")
        useJUnitPlatform {
            filter {
                includeTestsMatching("*Test")
                includeTestsMatching("*Tests")
                includeTestsMatching("*Spec")
            }

            /*
             * Performance tests are only really run during development.
             * They don't need to run in CI or as part of regular development.
             */
            excludeTags("performance")

            /*
             * Marking a test as `slow` will excluded it from being run as part of the regular CI system.
             */
            excludeTags("slow")
        }

        if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
            jvmArgs = listOf(
                "-Djava.awt.headless=true",
                "-Dtestfx.robot=glass",
                "-Dtestfx.headless=true",
                "-Dprism.order=sw",
                "-Dprism.text=t2k"
            )
        }

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
        }

        @Suppress("UnstableApiUsage")
        reports.junitXml.destination = file("${rootProject.buildDir}/test-results/${project.name}")
    }

    tasks.withType<JacocoReport> {
        @Suppress("UnstableApiUsage")
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    spotless {
        java {
            googleJavaFormat()
            removeUnusedImports()
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            @Suppress("INACCESSIBLE_TYPE")
            licenseHeaderFile(
                "${rootProject.rootDir}/config/spotless/bowler-kernel.license",
                spotlessLicenseHeaderDelimiter
            )
        }
    }

    checkstyle {
        toolVersion = checkstyleToolVersion
    }

    spotbugs {
        toolVersion = spotbugsToolVersion
        excludeFilter = file("${rootProject.rootDir}/config/spotbugs/spotbugs-excludeFilter.xml")
    }

    tasks.withType<SpotBugsTask> {
        @Suppress("UnstableApiUsage")
        reports {
            xml.isEnabled = false
            emacs.isEnabled = false
            html.isEnabled = true
        }
    }

    pmd {
        toolVersion = pmdToolVersion
        ruleSets = emptyList() // Needed so PMD only uses our custom ruleset
        ruleSetFiles = files("${rootProject.rootDir}/config/pmd/pmd-ruleset.xml")
    }
}

configure(kotlinProjects) {
    val kotlinVersion = property("kotlin.version") as String

    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jetbrains.dokka")
    }

    repositories {
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8", kotlinVersion))
        implementation(kotlin("reflect", kotlinVersion))
        implementation(
            group = "org.jetbrains.kotlinx",
            name = "kotlinx-coroutines-core",
            version = "1.0.0"
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable", "-progressive")
        }
    }

    val compileKotlin: KotlinCompile by tasks
    afterEvaluate {
        /*
         * Needed to configure kotlin to work correctly with the "java-library" plugin.
         * See:
         * https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_known_issues
         */
        pluginManager.withPlugin("java-library") {
            configurations {
                "apiElements" {
                    outgoing
                        .variants
                        .getByName("classes")
                        .artifact(
                            mapOf(
                                "file" to compileKotlin.destinationDir,
                                "type" to "java-classes-directory",
                                "builtBy" to compileKotlin
                            )
                        )
                }
            }
        }
    }

    spotless {
        kotlin {
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            @Suppress("INACCESSIBLE_TYPE")
            licenseHeaderFile(
                "${rootProject.rootDir}/config/spotless/bowler-kernel.license",
                spotlessLicenseHeaderDelimiter
            )
        }
    }

    detekt {
        toolVersion = detektToolVersion
        input = files("src/main/kotlin", "src/test/kotlin")
        parallel = true
        config = files("${rootProject.rootDir}/config/detekt/config.yml")
    }
}

configure(javaProjects + kotlinProjects) {
    val writePropertiesTask = tasks.create("writeProperties", WriteProperties::class) {
        val propFileDir = Paths.get(buildDir.path, "resources", "main").toFile().apply {
            mkdirs()
        }

        outputFile = Paths.get(propFileDir.path, "version.properties").toFile()
        property("version", version as String)
    }

    tasks.named("classes") {
        dependsOn(writePropertiesTask)
    }
}

configure(publishedProjects) {
    apply {
        plugin("com.jfrog.bintray")
        plugin("maven-publish")
        plugin("java-library")
    }

    val projectName = "bowler-kernel"

    task<Jar>("sourcesJar") {
        classifier = "sources"
        baseName = "$projectName-${this@configure.name.toLowerCase()}"
        from(sourceSets.main.get().allSource)
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        classifier = "javadoc"
        baseName = "$projectName-${this@configure.name.toLowerCase()}"
        from(tasks.dokka)
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
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_API_KEY")
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
                name = bowlerKernelVersion
                desc = "The heart of the Bowler stack."
            }
        }
    }
}

tasks.dokka {
    dependsOn(tasks.classes)
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

tasks.wrapper {
    gradleVersion = "5.4.1"
    distributionType = Wrapper.DistributionType.ALL
}
