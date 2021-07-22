plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(project(":gradle-test-core"))
    api("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
}
