package de.solugo.gradle.test.core

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class Executor(
    val result: BuildResult
) {
    companion object : GradleTest.Key<Executor>() {
        fun GradleTest.execute(vararg parameters: String, block: Executor.() -> Unit) = getOrSet(Executor) {
            Executor(
                result = GradleRunner.create().run {
                    withProjectDir(get(Directory).path.toFile())
                    withArguments(*(arrayOf("-s", "--include-build", File(".").absolutePath) + parameters))
                    withDebug(true)
                    build()
                }
            ).apply(block)
        }
    }

    val output; get() = result.output
}