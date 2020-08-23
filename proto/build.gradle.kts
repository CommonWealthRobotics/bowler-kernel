import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    java
    id("com.google.protobuf") version Versions.protobufPlugin
}

dependencies {
    api(group = "com.google.protobuf", name = "protobuf-java", version = Versions.protobufJava)
    api(group = "io.grpc", name = "grpc-all", version = Versions.grpc)
    api(group = "io.grpc", name = "grpc-kotlin-stub", version = Versions.grpcKotlin)
    api(group = "javax.annotation", name = "javax.annotation-api", version = Versions.javaxAnnotationAPI)
}

spotless {
    java {
        targetExclude("**/*")
    }
    kotlin {
        targetExclude("**/*")
    }
}

sourceSets {
    main {
        proto {
            srcDir("$projectDir/bowler-proto/src/proto")
        }
        java {
            srcDir("$buildDir/generated/source/proto/main/grpc")
            srcDir("$buildDir/generated/source/proto/main/java")
            srcDir("$buildDir/generated/source/proto/main/grpckt")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.protobufJava}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.grpc}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.grpcKotlin}"
        }
    }
    generateProtoTasks {
        all().forEach {
            // Disable caching because of https://github.com/google/protobuf-gradle-plugin/issues/180
            it.doFirst { delete(it.outputs) }
            it.outputs.upToDateWhen { false }

            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
