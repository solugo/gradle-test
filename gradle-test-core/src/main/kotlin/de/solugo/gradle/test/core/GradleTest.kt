package de.solugo.gradle.test.core

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class GradleTest {

    companion object {
        operator fun invoke(block: GradleTest.() -> Unit) {
            GradleTest().apply {
                block()
                close()
            }
        }
    }

    private val context = hashMapOf<Key<*>, Any>()

    fun <T : Any> set(key: Key<T>, value: T) = synchronized(context) {
        context[key] = value
    }

    @Suppress("unchecked_cast")
    fun <T : Any> get(key: Key<T>) = synchronized(context) {
        getOrNull(key) ?: error("Did not find entry for key ${key::class.qualifiedName}")
    }

    @Suppress("unchecked_cast")
    fun <T : Any> getOrSet(key: Key<T>, supplier: () -> T): T = synchronized(context) {
        context.computeIfAbsent(key) { supplier() } as T
    }

    @Suppress("unchecked_cast")
    fun <T : Any> getOrNull(key: Key<T>) = synchronized(context) {
        context.get(key) as T?
    }

    fun close() {
        context.values.forEach {
            (it as? AutoCloseable)?.close()
        }
    }


    fun File.gradle(vararg parameters: String, block: BuildResult.() -> Unit) {
        GradleRunner.create().run {
            withProjectDir(this@gradle)
            withArguments(*(arrayOf("-s", "--include-build", File(".").absolutePath) + parameters))
            withDebug(true)
            build().block()
        }
    }

    abstract class Key<T : Any>

    class Context {
        private val context = hashMapOf<Key<*>, Any>()

        fun <T : Any> set(key: Key<T>, value: T) = synchronized(context) {
            context[key] = value
        }

        @Suppress("unchecked_cast")
        fun <T : Any> get(key: Key.Creatable<T>): T = synchronized(context) {
            context.computeIfAbsent(key) {
                key.supplier(this)
            } as T
        }

        fun <T : Any> getOrNull(key: Key<T>) = synchronized(context) {
            context.get(key)
        }

        fun close() {
            context.values.forEach {
                (it as? AutoCloseable)?.close()
            }
        }

        abstract class Key<T : Any> {
            abstract class Creatable<T : Any>(val supplier: Context.() -> T) : Key<T>()
        }

    }
}
