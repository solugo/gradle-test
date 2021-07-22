import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.20" apply false
    id("org.jetbrains.dokka") version "1.5.0" apply false
    id("maven-publish")
    id("signing")
}

group = "de.solugo.gradle.test"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    pluginManager.withPlugin("java") {
        tasks.withType<JavaCompile> {
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }

    pluginManager.withPlugin("org.jetbrains.dokka") {
        tasks.withType<DokkaTask> {
            logging.captureStandardOutput(LogLevel.ERROR)
        }
        tasks.create<Jar>("javadocJar") {
            dependsOn("dokkaJavadoc")
            from(tasks.getByName("dokkaJavadoc"))
            archiveClassifier.set("javadoc")

            artifacts.add("archives", this)
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        val sourceSets = extensions.getByType<SourceSetContainer>()
        tasks.create<Jar>("sourcesJar") {
            from(sourceSets.getByName("main").allSource)
            archiveClassifier.set("sources")

            artifacts.add("archives", this)
        }
        tasks.withType<KotlinJvmCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            val repositoryUrl = properties["repositoryUrl"]?.toString()
            val repositoryUsername = properties["repositoryUsername"]?.toString()
            val repositoryPassword = properties["repositoryPassword"]?.toString()

            val signingKey = properties["signingKey"]?.toString()
            val signingPassword = properties["signingPassword"]?.toString()

            publications {
                create<MavenPublication>("main") {
                    from(components.getByName("kotlin"))

                    pluginManager.withPlugin("signing") {
                        extensions.configure<SigningExtension> {
                            setRequired { gradle.taskGraph.hasTask("publish") }
                            useInMemoryPgpKeys(signingKey, signingPassword)
                            sign(this@create)
                        }
                    }

                    artifact(tasks.findByName("javadocJar"))
                    artifact(tasks.findByName("sourcesJar"))

                    pom {
                        name.set("$groupId:$artifactId")
                        description.set("Gradle test util library")
                        url.set("https://github.com/solugo/gradle-test")
                        developers {
                            developer {
                                name.set("Frederic Kneier")
                                email.set("frederic@kneier.net")
                            }
                        }
                        scm {
                            url.set("https://github.com/solugo/gradle-test/tree/main")
                            connection.set("https://github.com/solugo/gradle-test.git")
                            developerConnection.set("git@github.com:solugo/gradle-test.git")
                        }
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                    }
                }
            }
            repositories {
                if (repositoryUrl != null && repositoryUsername != null && repositoryPassword != null) {
                    maven {
                        url = uri(repositoryUrl)
                        credentials {
                            username = repositoryUsername
                            password = repositoryPassword
                        }
                    }
                }
            }
        }
    }

}
