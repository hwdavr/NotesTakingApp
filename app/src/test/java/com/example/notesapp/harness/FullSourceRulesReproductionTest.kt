package com.example.notesapp.harness

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class FullSourceRulesReproductionTest {
    @Test
    fun fullSourceRulesReportNoViolations() {
        val projectRoot = findProjectRoot()
        val process = ProcessBuilder("bash", "harness/scripts/check-full-source-rules.sh")
            .directory(projectRoot)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        assertEquals("Full-source rules output:\n$output", 0, exitCode)
    }

    private fun findProjectRoot(): File {
        var candidate = File(System.getProperty("user.dir").orEmpty()).absoluteFile
        while (true) {
            if (File(candidate, "harness/scripts/check-full-source-rules.sh").isFile) {
                return candidate
            }
            candidate = candidate.parentFile ?: break
        }
        throw AssertionError("Could not locate the project root from ${System.getProperty("user.dir").orEmpty()}")
    }
}
