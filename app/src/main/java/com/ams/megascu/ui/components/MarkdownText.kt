package com.ams.megascu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class BulletItem(val depth: Int, val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class BlockQuote(val text: String) : MarkdownBlock
    data class CodeBlock(val code: String) : MarkdownBlock
    data object Divider : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
}

/**
 * Parses markdown inline formatting (bold, italic, inline code, strikethrough, links).
 */
fun parseMarkdownInline(
    text: String,
    primaryColor: Color,
    codeBgColor: Color,
    codeTextColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length
        while (i < len) {
            // Bold & Italic: ***text***
            if (i + 2 < len && text[i] == '*' && text[i + 1] == '*' && text[i + 2] == '*') {
                val end = text.indexOf("***", i + 3)
                if (end != -1) {
                    val inner = text.substring(i + 3, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                    append(inner)
                    pop()
                    i = end + 3
                    continue
                }
            }

            // Bold: **text** or __text__
            if (i + 1 < len && ((text[i] == '*' && text[i + 1] == '*') || (text[i] == '_' && text[i + 1] == '_'))) {
                val delim = text.substring(i, i + 2)
                val end = text.indexOf(delim, i + 2)
                if (end != -1) {
                    val inner = text.substring(i + 2, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(inner)
                    pop()
                    i = end + 2
                    continue
                }
            }

            // Inline Code: `code`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    val inner = text.substring(i + 1, end)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBgColor,
                            color = codeTextColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    append(" $inner ")
                    pop()
                    i = end + 1
                    continue
                }
            }

            // Strikethrough: ~~text~~
            if (i + 1 < len && text[i] == '~' && text[i + 1] == '~') {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    val inner = text.substring(i + 2, end)
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(inner)
                    pop()
                    i = end + 2
                    continue
                }
            }

            // Italic: *text* or _text_
            if (text[i] == '*' || text[i] == '_') {
                val delim = text[i]
                val end = text.indexOf(delim, i + 1)
                if (end != -1 && end > i + 1) {
                    val inner = text.substring(i + 1, end)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(inner)
                    pop()
                    i = end + 1
                    continue
                }
            }

            // Markdown Link: [text](url) -> display text in primary color with underline
            if (text[i] == '[') {
                val closeBracket = text.indexOf(']', i + 1)
                if (closeBracket != -1 && closeBracket + 1 < len && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen != -1) {
                        val linkText = text.substring(i + 1, closeBracket)
                        pushStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        append(linkText)
                        pop()
                        i = closeParen + 1
                        continue
                    }
                }
            }

            append(text[i])
            i++
        }
    }
}

/**
 * Parses raw markdown text into structural blocks.
 */
private fun parseMarkdownBlocks(rawMarkdown: String): List<MarkdownBlock> {
    val lines = rawMarkdown.replace("\r\n", "\n").replace("\r", "\n").split("\n")
    val blocks = mutableListOf<MarkdownBlock>()

    var inCodeFence = false
    val codeFenceBuffer = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()

        // Code block fence toggle
        if (trimmed.startsWith("```")) {
            if (inCodeFence) {
                blocks.add(MarkdownBlock.CodeBlock(codeFenceBuffer.toString().trimEnd()))
                codeFenceBuffer.clear()
                inCodeFence = false
            } else {
                inCodeFence = true
                codeFenceBuffer.clear()
            }
            continue
        }

        if (inCodeFence) {
            codeFenceBuffer.append(line).append("\n")
            continue
        }

        if (trimmed.isEmpty()) {
            continue
        }

        // Horizontal Rule (---, ***, ___)
        if (trimmed.matches(Regex("^([-*_])\\1{2,}$"))) {
            blocks.add(MarkdownBlock.Divider)
            continue
        }

        // Headers: # H1, ## H2, ### H3, #### H4
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            val headerContent = trimmed.drop(level).trim()
            blocks.add(MarkdownBlock.Header(level.coerceIn(1, 6), headerContent))
            continue
        }

        // Blockquotes: > Quote
        if (trimmed.startsWith(">")) {
            val quoteContent = trimmed.removePrefix(">").trim()
            blocks.add(MarkdownBlock.BlockQuote(quoteContent))
            continue
        }

        // Bullet list item: -, *, +, or unicode bullets •
        val leadingSpaces = line.takeWhile { it == ' ' || it == '\t' }.length
        val indentDepth = (leadingSpaces / 2).coerceAtMost(3)

        val bulletMatch = Regex("^[-*+•]\\s+(.*)$").find(trimmed)
        if (bulletMatch != null) {
            val itemText = bulletMatch.groupValues[1].trim()
            blocks.add(MarkdownBlock.BulletItem(depth = indentDepth, text = itemText))
            continue
        }

        // Numbered list item: 1. or 1)
        val numberedMatch = Regex("^(\\d+)[.)]\\s+(.*)$").find(trimmed)
        if (numberedMatch != null) {
            val num = numberedMatch.groupValues[1]
            val itemText = numberedMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.NumberedItem(number = num, text = itemText))
            continue
        }

        // Regular paragraph
        blocks.add(MarkdownBlock.Paragraph(trimmed))
    }

    if (inCodeFence && codeFenceBuffer.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeFenceBuffer.toString().trimEnd()))
    }

    return blocks
}

/**
 * Rich Markdown display composable specifically designed for Changelog, Release Notes,
 * and Update dialogues in MegasCU adhering to Material 3 Expressive aesthetics.
 */
@Composable
fun MarkdownChangelog(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
    val codeTextColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    val (fontSize, fontWeight, color) = when (block.level) {
                        1 -> Triple(15.sp, FontWeight.ExtraBold, primaryColor)
                        2 -> Triple(14.sp, FontWeight.Bold, primaryColor)
                        3 -> Triple(13.sp, FontWeight.SemiBold, onSurfaceColor)
                        else -> Triple(12.5.sp, FontWeight.SemiBold, onSurfaceVariantColor)
                    }
                    val styledText = parseMarkdownInline(
                        text = block.text,
                        primaryColor = primaryColor,
                        codeBgColor = codeBgColor,
                        codeTextColor = codeTextColor
                    )
                    Text(
                        text = styledText,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = color,
                        lineHeight = (fontSize.value + 4).sp
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.depth * 12 + 2).dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, end = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        val styledText = parseMarkdownInline(
                            text = block.text,
                            primaryColor = primaryColor,
                            codeBgColor = codeBgColor,
                            codeTextColor = codeTextColor
                        )
                        Text(
                            text = styledText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            ),
                            color = onSurfaceVariantColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = primaryColor,
                            modifier = Modifier.width(20.dp)
                        )
                        val styledText = parseMarkdownInline(
                            text = block.text,
                            primaryColor = primaryColor,
                            codeBgColor = codeBgColor,
                            codeTextColor = codeTextColor
                        )
                        Text(
                            text = styledText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            ),
                            color = onSurfaceVariantColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryColor.copy(alpha = 0.7f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val styledText = parseMarkdownInline(
                            text = block.text,
                            primaryColor = primaryColor,
                            codeBgColor = codeBgColor,
                            codeTextColor = codeTextColor
                        )
                        Text(
                            text = styledText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = onSurfaceVariantColor.copy(alpha = 0.9f)
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = codeBgColor
                    ) {
                        Text(
                            text = block.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            color = codeTextColor,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    val styledText = parseMarkdownInline(
                        text = block.text,
                        primaryColor = primaryColor,
                        codeBgColor = codeBgColor,
                        codeTextColor = codeTextColor
                    )
                    Text(
                        text = styledText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = onSurfaceVariantColor
                    )
                }
            }
        }
    }
}
