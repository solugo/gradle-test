package de.solugo.gradle.test.git

import de.solugo.gradle.test.core.Directory
import de.solugo.gradle.test.core.GradleTest
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class Git(
    val repository: Repository,
) : AutoCloseable {

    val git = org.eclipse.jgit.api.Git.wrap(repository)

    companion object : GradleTest.Key<Git>() {
        fun GradleTest.git(block: Git.() -> Unit = {}) = getOrSet(Git) {
            Git(
                repository = FileRepositoryBuilder().run {
                    gitDir = get(Directory).path.resolve(".git").toFile()
                    build()
                }.apply {
                    create()
                }
            ).apply(block)
        }
    }

    fun commit(message: String, add: Boolean = true) {
        if (add) {
            git.add().apply {
                addFilepattern(".")
                call()
            }
        }
        git.commit().apply {
            this.message = message
            setAllowEmpty(true)
            call()
        }
    }

    fun tag(name: String) {
        git.tag().apply {
            this.name = name
            call()
        }
    }

    override fun close() {
        git.close()
        repository.close()
    }
}