package de.solugo.gradle.test.core

import io.github.classgraph.ClassGraph
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.concurrent.thread

class Directory(
    val path: Path,
    val deleteOnFinish: Boolean = false,
) : AutoCloseable {

    companion object Helper : GradleTest.Key<Directory>() {
        fun GradleTest.directory(block: Directory.() -> Unit = {}) = get(Directory).apply(block)

        fun GradleTest.withTemporaryDirectory(
            block: Directory.() -> Unit = {},
        ) = Directory(Files.createTempDirectory("gradleProject"), deleteOnFinish = true).apply {
            set(Directory, this)
            block()
        }

        fun GradleTest.path(
            path: String,
            block: Path.() -> Unit = {},
        ) = get(Directory).path.resolve(path).apply(block)

        fun GradleTest.file(
            path: String,
            block: File.() -> Unit = {},
        ) = get(Directory).path.resolve(path).toFile().apply(block)

        fun Path.extractFileFromClasspath(path: String, name: String = path.substringAfterLast("/")) {
            resolve(name).also { target ->
                Files.createDirectories(target.parent)
                Files.newOutputStream(target).use { outputStream ->
                    checkNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream(path)) {
                        "Could not find resource $path"
                    }.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }

        fun Path.extractDirectoryFromClasspath(
            vararg paths: String,
        ) {
            paths.forEach { path ->
                ClassGraph().acceptPaths(path).scan().allResources.forEach { resource ->
                    extractFileFromClasspath(resource.path, resource.path.removePrefix(path).removePrefix("/"))
                }
            }
        }
    }

    override fun close() {
        if (deleteOnFinish) Files.walk(path).sorted(Comparator.reverseOrder()).forEach {
            it.toFile().delete()
        }
    }

}