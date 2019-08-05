import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.spotbugs.SpotBugsTask
import info.solidsoft.gradle.pitest.PitestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths

plugins {
    id("com.diffplug.gradle.spotless") version "3.23.1"
    id("org.jlleitschuh.gradle.ktlint") version "7.3.0"
    id("com.github.spotbugs") version "1.7.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
    id("com.jfrog.bintray") version "1.8.3"
    id("org.jetbrains.dokka") version "0.9.18"
    id("com.adarshr.test-logger") version "1.6.0"
    id("info.solidsoft.pitest") version "1.4.0"
    `maven-publish`
    `java-library`
    jacoco
    pmd
    checkstyle
}

val bowlerKernelProject = project(":bowler-kernel")
val bowlerKernelCadCoreProject = project(":bowler-kernel:cad-core")
val bowlerKernelCadVitaminsProject = project(":bowler-kernel:cad-vitamins")
val bowlerKernelConfigProject = project(":bowler-kernel:config")
val bowlerKernelGitFSProject = project(":bowler-kernel:gitfs")
val bowlerKernelHardwareProject = project(":bowler-kernel:hardware")
val bowlerKernelKinematicsProject = project(":bowler-kernel:kinematics")
val bowlerKernelKinematicsFactoriesProject = project(":bowler-kernel:kinematics-factories")
val bowlerKernelLoggingProject = project(":bowler-kernel:logging")
val bowlerKernelScriptingProject = project(":bowler-kernel:scripting")
val bowlerKernelUtilProject = project(":bowler-kernel:util")
val bowlerKernelVitaminsProject = project(":bowler-kernel:vitamins")

val kotlinProjects = setOf(
    bowlerKernelProject,
    bowlerKernelCadCoreProject,
    bowlerKernelCadVitaminsProject,
    bowlerKernelConfigProject,
    bowlerKernelGitFSProject,
    bowlerKernelHardwareProject,
    bowlerKernelKinematicsProject,
    bowlerKernelKinematicsFactoriesProject,
    bowlerKernelLoggingProject,
    bowlerKernelScriptingProject,
    bowlerKernelUtilProject,
    bowlerKernelVitaminsProject
)

val javaProjects = setOf<Project>() + kotlinProjects

val publishedProjects = setOf(
    bowlerKernelCadCoreProject,
    bowlerKernelCadVitaminsProject,
    bowlerKernelConfigProject,
    bowlerKernelGitFSProject,
    bowlerKernelHardwareProject,
    bowlerKernelKinematicsProject,
    bowlerKernelKinematicsFactoriesProject,
    bowlerKernelLoggingProject,
    bowlerKernelScriptingProject,
    bowlerKernelUtilProject,
    bowlerKernelVitaminsProject
)

val pitestProjects = setOf(
    bowlerKernelCadCoreProject,
    bowlerKernelCadVitaminsProject,
    bowlerKernelGitFSProject,
    bowlerKernelHardwareProject,
    bowlerKernelKinematicsProject,
    bowlerKernelKinematicsFactoriesProject,
    bowlerKernelScriptingProject,
    bowlerKernelVitaminsProject
)

val spotlessLicenseHeaderDelimiter = "(@|package|import)"

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/staging/")
    }

    configurations.maybeCreate("pitest")

    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlin.version") as String))
        "pitest"("org.pitest:pitest-junit5-plugin:0.9")
    }
}

allprojects {
    version = property("bowler-kernel.version") as String
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
        maven("https://dl.bintray.com/47deg/helios")
        maven("https://dl.bintray.com/s1m0nw1/KtsRunner")
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = property("jacoco-tool.version") as String
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
            ktlint(property("ktlint.version") as String)
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

configure(javaProjects) {
    apply {
        plugin("java")
        plugin("jacoco")
        plugin("checkstyle")
        plugin("com.github.spotbugs")
        plugin("pmd")
    }

    dependencies {
        testCompile(
            group = "org.junit.jupiter",
            name = "junit-jupiter",
            version = property("junit-jupiter.version") as String
        )

        testRuntime(
            group = "org.junit.platform",
            name = "junit-platform-launcher",
            version = property("junit-platform-launcher.version") as String
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
            These tests just test performance and should not run in CI.
             */
            excludeTags("performance")

            /*
            These tests are too slow to run in CI.
             */
            excludeTags("slow")

            /*
            These tests need some sort of software that can't be reasonably installed on CI servers.
             */
            excludeTags("needsSpecialSoftware")
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
        toolVersion = property("checkstyle-tool.version") as String
    }

    spotbugs {
        toolVersion = property("spotbugs-tool.version") as String
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
        toolVersion = property("pmd-tool.version") as String
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
            version = property("kotlin-coroutines.version") as String
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable", "-progressive")
        }
    }

    // val compileKotlin: KotlinCompile by tasks
    // afterEvaluate {
    //     /*
    //      * Needed to configure kotlin to work correctly with the "java-library" plugin.
    //      * See:
    //      * https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_known_issues
    //      */
    //     pluginManager.withPlugin("java-library") {
    //         configurations {
    //             "apiElements" {
    //                 outgoing
    //                     .variants
    //                     .getByName("classes")
    //                     .artifact(
    //                         mapOf(
    //                             "file" to compileKotlin.destinationDir,
    //                             "type" to "java-classes-directory",
    //                             "builtBy" to compileKotlin
    //                         )
    //                     )
    //             }
    //         }
    //     }
    // }

    spotless {
        kotlin {
            ktlint(property("ktlint.version") as String)
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
        toolVersion = property("detekt-tool.version") as String
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

configure(pitestProjects) {
    apply {
        plugin("info.solidsoft.pitest")
    }

    pitest {
        testPlugin = "junit5"
        threads = 2
        avoidCallsTo = setOf("kotlin.jvm.internal", "kotlinx.coroutines", "kotlin.ResultKt")
        timeoutConstInMillis = 10000
    }
}

tasks.withType<PitestTask> {
    onlyIf { project in pitestProjects }
}

configure(publishedProjects) {
    apply {
        plugin("com.jfrog.bintray")
        plugin("maven-publish")
        plugin("java-library")
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
                name = property("bowler-kernel.version") as String
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
    gradleVersion = rootProject.property("gradle-wrapper.version") as String
    distributionType = Wrapper.DistributionType.ALL
}
