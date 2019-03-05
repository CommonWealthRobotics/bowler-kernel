import Kinematics_chef_gradle.Strings.spotlessLicenseHeaderDelimiter
import Kinematics_chef_gradle.Versions.kinematicsChefVersion
import Kinematics_chef_gradle.Versions.ktlintVersion
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.spotbugs.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.util.GFileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco
    pmd
    id("com.diffplug.gradle.spotless") version "3.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "6.3.1"
    id("com.github.spotbugs") version "1.6.5"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.3"
    `java-library`
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

object Versions {
    const val ktlintVersion = "0.29.0"
    const val kinematicsChefVersion = "0.0.16"
}

allprojects {
    version = kinematicsChefVersion
    group = "com.neuronrobotics"
}

val coreProject = project(":core")

val kotlinProjects = setOf(
        coreProject
)

val javaProjects = setOf<Project>() + kotlinProjects

val publishedProjects = setOf<Project>() + kotlinProjects

object Strings {
    const val spotlessLicenseHeaderDelimiter = "(@|package|import)"
}

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlin.version") as String))
    }
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
    }

    repositories {
        jcenter()
        mavenCentral()
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = "0.8.2"
        }
    }

    tasks.withType<Test> {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
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

configure(javaProjects) {
    apply {
        plugin("java")
        plugin("jacoco")
        plugin("checkstyle")
        plugin("com.github.spotbugs")
        plugin("pmd")
    }

    dependencies {
        fun junitJupiter(name: String, version: String = "5.2.0") =
                create(group = "org.junit.jupiter", name = name, version = version)

        fun testFx(name: String, version: String = "4.0.+") =
                create(group = "org.testfx", name = name, version = version)

        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(junitJupiter(name = "junit-jupiter-params"))
        "testCompile"(testFx(name = "testfx-core", version = "4.0.7-alpha"))
        "testCompile"(testFx(name = "testfx-junit5", version = "4.0.6-alpha"))
        "testCompile"(group = "com.google.guava", name = "guava-testlib", version = "23.0")
        "testCompile"(group = "org.mockito", name = "mockito-core", version = "2.12.0")

        "testRuntime"(
                group = "org.junit.platform",
                name = "junit-platform-launcher",
                version = "1.0.0"
        )
        "testRuntime"(testFx(name = "openjfx-monocle", version = "8u76-b04"))
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
                    "${rootProject.rootDir}/config/spotless/kinematicschef.license",
                    spotlessLicenseHeaderDelimiter
            )
        }
    }

    checkstyle {
        toolVersion = "8.15"
    }

    spotbugs {
        toolVersion = "3.1.9"
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
        toolVersion = "6.9.0"
        ruleSets = emptyList() // Needed so PMD only uses our custom ruleset
        ruleSetFiles = files("${rootProject.rootDir}/config/pmd/pmd-ruleset.xml")
    }
}

configure(kotlinProjects) {
    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
    }

    val kotlinVersion = property("kotlin.version") as String

    dependencies {
        // Weird syntax, see: https://github.com/gradle/kotlin-dsl/issues/894
        "compile"(kotlin("stdlib", kotlinVersion))
        "compile"(kotlin("reflect", kotlinVersion))
        "compile"(
                group = "org.jetbrains.kotlinx",
                name = "kotlinx-coroutines-core",
                version = "1.0.0"
        )

        "testCompile"(kotlin("test", kotlinVersion))
        "testCompile"(kotlin("test-junit", kotlinVersion))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs =
                    listOf("-Xjvm-default=enable", "-progressive", "-XXLanguage:+InlineClasses")
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
                    "${rootProject.rootDir}/config/spotless/kinematicschef.license",
                    spotlessLicenseHeaderDelimiter
            )
        }
    }

    detekt {
        toolVersion = "1.0.0-RC12"
        input = files(
                "src/main/kotlin",
                "src/test/kotlin"
        )
        parallel = true
        config = files("${rootProject.rootDir}/config/detekt/config.yml")
    }

    checkstyle {
        configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
    }
}

configure(publishedProjects) {
    apply {
        plugin("com.jfrog.bintray")
        plugin("maven-publish")
        plugin("java-library")
        plugin("com.github.johnrengelman.shadow")
    }

    task<Jar>("sourcesJar") {
        from(sourceSets.main.get().allSource)
        classifier = "sources"
        baseName = "kinematics-chef-${this@configure.name.toLowerCase()}"
    }

    task<Jar>("javadocJar") {
        from(tasks.javadoc)
        classifier = "javadoc"
        baseName = "kinematics-chef-${this@configure.name.toLowerCase()}"
    }

    tasks {
        "shadowJar"(ShadowJar::class) {
            baseName = "kinematics-chef-${this@configure.name.toLowerCase()}"
        }
    }

    val publicationName = "publication-kinematics-chef-${name.toLowerCase()}"

    publishing {
        publications {
            create<MavenPublication>(publicationName) {
                artifactId = "kinematics-chef-${this@configure.name.toLowerCase()}"
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                artifact(tasks["shadowJar"])
            }
        }
    }

    bintray {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_API_KEY")
        setPublications(publicationName)
        with(pkg) {
            repo = "maven-artifacts"
            name = "kinematics-chef"
            userOrg = "commonwealthrobotics"
            publish = true
            setLicenses("LGPL-3.0")
            vcsUrl = "https://github.com/CommonWealthRobotics/kinematics-chef.git"
            githubRepo = "https://github.com/CommonWealthRobotics/kinematics-chef"
            with(version) {
                name = kinematicsChefVersion
                desc = "Cooking up kinematics solutions."
            }
        }
    }
}

val checkTask = tasks.maybeCreate("check", Task::class.java).apply {
    description = "Check all sub-projects"
    group = LifecycleBasePlugin.VERIFICATION_GROUP
}

val buildTask = tasks.maybeCreate("build", Task::class.java).apply {
    description = "Build all sub-projects"
    group = LifecycleBasePlugin.BUILD_GROUP
    dependsOn(checkTask)
}

configure(javaProjects + kotlinProjects) {
    checkTask.dependsOn(tasks.getByName("check"))
    buildTask.dependsOn(tasks.getByName("build"))
}

tasks.wrapper {
    gradleVersion = "5.0"
    distributionType = Wrapper.DistributionType.ALL

    doLast {
        /*
         * Copy the properties file into the buildSrc project.
         * Related issues:
         *
         * https://youtrack.jetbrains.com/issue/KT-14895
         * https://youtrack.jetbrains.com/issue/IDEA-169717
         * https://youtrack.jetbrains.com/issue/IDEA-153336
         */
        val buildSrcWrapperDir = File(rootDir, "buildSrc/gradle/wrapper")
        GFileUtils.mkdirs(buildSrcWrapperDir)
        copy {
            from(propertiesFile)
            into(buildSrcWrapperDir)
        }
    }
}

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] project extension.
 */
fun Project.`publishing`(configure: org.gradle.api.publish.PublishingExtension.() -> Unit) =
        extensions.configure("publishing", configure)

/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit) =
        extensions.configure("checkstyle", configure)

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention
    get() = convention.getPluginByName("java")

/**
 * Configures the [kotlin][org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension] project extension.
 */
fun Project.`kotlin`(configure: org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension.() -> Unit): Unit =
        extensions.configure("kotlin", configure)

/**
 * Configures the [detekt][io.gitlab.arturbosch.detekt.extensions.DetektExtension] extension.
 */
fun org.gradle.api.Project.`detekt`(configure: io.gitlab.arturbosch.detekt.extensions.DetektExtension.() -> Unit): Unit =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("detekt", configure)
