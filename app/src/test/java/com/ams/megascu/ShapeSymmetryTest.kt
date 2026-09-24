package com.ams.megascu

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regression guard for the project's UI rule: interactive/card shapes must use a single radius.
 * The only allowed explicit corner-by-corner shape is a standard M3 bottom sheet (28dp top corners).
 */
class ShapeSymmetryTest {

    @Test
    fun sourceHasNoUnequalInteractiveRoundedCornerShapes() {
        val sourceRoot = File("src/main/java")
        val violations = Regex(
            """RoundedCornerShape\(([^)]*)\)""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )

        val cornerNamed = Regex("""\b(?:topStart|topEnd|bottomStart|bottomEnd)\s*=""")

        val bad = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                violations.findAll(text).mapNotNull { match ->
                    val body = match.groupValues[1].replace(Regex("""\s+"""), " ").trim()
                    if (!cornerNamed.containsMatchIn(body)) return@mapNotNull null
                    val normalized = body.replace(" ", "")
                    val cornerValues = Regex(
                        """(topStart|topEnd|bottomStart|bottomEnd)=([^,]+)"""
                    ).findAll(normalized).associate { it.groupValues[1] to it.groupValues[2] }
                    val allNamedCornersEqual = cornerValues.isNotEmpty() && cornerValues.values.distinct().size == 1
                    val isBottomSheet = cornerValues == mapOf(
                        "topStart" to "28.dp",
                        "topEnd" to "28.dp"
                    )
                    val isGroupedCard = cornerValues.size == 4 &&
                        cornerValues["topStart"] == cornerValues["topEnd"] &&
                        cornerValues["bottomStart"] == cornerValues["bottomEnd"]
                    if (allNamedCornersEqual || isBottomSheet || isGroupedCard) {
                        null
                    } else {
                        "${file.path}:${text.substring(0, match.range.first).count { it == '\n' } + 1}: $body"
                    }
                }
            }
            .toList()

        assertTrue("Se detectaron shapes de esquinas desiguales: $bad", bad.isEmpty())
    }
}
