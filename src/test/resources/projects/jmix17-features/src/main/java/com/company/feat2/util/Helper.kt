// Test data, intentionally Kotlin: exercises the Kotlin detector of the analyzer.
// The tool must count this file and warn in the report that Kotlin sources are not analyzed.
package com.company.feat2.util

object Helper {
    fun normalize(value: String): String = value.trim()
}
