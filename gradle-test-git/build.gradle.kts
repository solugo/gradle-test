plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(project(":gradle-test-core"))
    api("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")
}
