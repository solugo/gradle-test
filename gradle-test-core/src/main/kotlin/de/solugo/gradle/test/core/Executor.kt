package de.solugo.gradle.test.core

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import java.io.File

class Executor(
    val result: BuildResult
) {
    companion object : GradleTest.Key<Executor>() {
        fun GradleTest.execute(vararg parameters: String, block: Executor.() -> Unit) = getOrSet(Executor) {
            Executor(
                result = try {
                    GradleRunner.create().run {
                        withProjectDir(get(Directory).path.toFile())
                        System.getenv()
                        withArguments(*(arrayOf("-s", "--include-build", File(".").absolutePath) + parameters))
                        withDebug(true)
                        build()
                    }
                } catch (ex: UnexpectedBuildFailure) {
                    ex.buildResult
                }
            ).apply(block)
        }
    }

    val output; get() = result.output
}