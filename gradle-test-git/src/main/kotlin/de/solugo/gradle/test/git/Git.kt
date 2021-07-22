package de.solugo.gradle.test.git

import de.solugo.gradle.test.core.Directory
import de.solugo.gradle.test.core.GradleTest

class Git(
    val git: org.eclipse.jgit.api.Git
) {
    companion object : GradleTest.Key<Git>() {
        fun GradleTest.git(block: Git.() -> Unit = {}) = getOrSet(Git) {
            Git(
                git = org.eclipse.jgit.api.Git.init().run {
                    val dir = get(Directory).path.toFile()
                    setDirectory(dir)
                    call().apply {
                        dir.resolve(".gitignore").writeText("/.gradle")
                    }
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
}