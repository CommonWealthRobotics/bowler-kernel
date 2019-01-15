plugins {
    `java-library`
}

description = "Various utilities the kernel uses."

dependencies {
    api(group = "com.google.guava", name = "guava", version = "27.0.1-jre")
}
