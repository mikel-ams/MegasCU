package com.ams.megascu.data.ussd

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.roundToLong

data class ParsedPlanData(
    val balanceCup: Double? = null,
    val dataMb: Long? = null,
    val dataLteMb: Long? = null,
    val bonusMb: Long? = null,
    val minutesStr: String? = null,
    val sms: Int? = null,
    val dataDays: Int? = null,
    val minutesDays: Int? = null,
    val smsDays: Int? = null,
    val nextRechargeDateStr: String? = null,
    val nextRechargeDays: Int? = null,
    val bonusDays: Int? = null
)

object EtecsaUssdParser {

    private val GENERAL_DATE_REGEX = Pattern.compile(
        """\b(\d{1,2}[-/.]\d{1,2}[-/.]\d{2,4})\b""",
        Pattern.CASE_INSENSITIVE
    )

    private val MINUTES_REGEX = Pattern.compile(
        """\b(\d{1,4}(?::\d{2})?)\s*(?:minutos?|mins?|min)\b""",
        Pattern.CASE_INSENSITIVE
    )

    private val SMS_REGEX = Pattern.compile(
        """\b(\d{1,5})\s*(?:sms|mensajes?)\b(?!\s+de\s+confirm)""",
        Pattern.CASE_INSENSITIVE
    )

    private val CUP_BALANCE_REGEX = Pattern.compile(
        """(?:su\s+)?saldo(?:\s+(?:principal|actual|disponible))?(?:\s+(?:es\s+de|es))?\s*[:=]?\s*(\d+(?:[.,]\d+)?)\s*(?:CUP\b)?""",
        Pattern.CASE_INSENSITIVE
    )

    private val CUP_PREFIX_BALANCE_REGEX = Pattern.compile(
        """(?:^|\bCUP)\s*[:=]\s*(\d+(?:[.,]\d+)?)\s*CUP\b""",
        Pattern.CASE_INSENSITIVE
    )

    private val DATA_MB_REGEX = Pattern.compile(
        "([0-9]+(?:[.,][0-9]+)?)\\s*(MB|GB)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val DAYS_REGEX = Pattern.compile(
        "([0-9]+)\\s*(?:d[ií]as?\\.?)",
        Pattern.CASE_INSENSITIVE
    )

    fun parseUssdResponse(response: String, ussdCode: String? = null): ParsedPlanData {
        var balance: Double? = null
        var dataMb: Long? = null
        var dataLteMb: Long? = null
        var bonusMb: Long? = null
        var minutesStr: String? = null
        var sms: Int? = null
        var dataDays: Int? = null
        var minutesDays: Int? = null
        var smsDays: Int? = null
        var nextRechargeDateStr: String? = null
        var nextRechargeDays: Int? = null
        var bonusDays: Int? = null

        val balanceMatcher = CUP_BALANCE_REGEX.matcher(response)
        if (balanceMatcher.find()) {
            balance = balanceMatcher.group(1)?.replace(',', '.')?.toDoubleOrNull()
        } else {
            val cupPrefixMatcher = CUP_PREFIX_BALANCE_REGEX.matcher(response)
            if (cupPrefixMatcher.find()) {
                balance = cupPrefixMatcher.group(1)?.replace(',', '.')?.toDoubleOrNull()
            }
        }
        
        val minutesMatcher = MINUTES_REGEX.matcher(response)
        if (minutesMatcher.find()) {
            minutesStr = minutesMatcher.group(1)
        }
        
        val smsMatcher = SMS_REGEX.matcher(response)
        if (smsMatcher.find()) {
            sms = smsMatcher.group(1)?.toIntOrNull()
        }

        val dataMatcher = DATA_MB_REGEX.matcher(response)
        while (dataMatcher.find()) {
            val amountStr = dataMatcher.group(1) ?: continue
            val unit = dataMatcher.group(2)?.uppercase() ?: "MB"
            val rawValue = amountStr.replace(',', '.').toDoubleOrNull() ?: continue
            val mbValue = if (unit == "GB") (rawValue * 1024.0).toLong() else rawValue.roundToLong()
            
            val matchStart = dataMatcher.start()
            val matchEnd = dataMatcher.end()
            val delimiters = charArrayOf(',', ';', '+', '\n', '\r')
            val lastDelimBefore = response.substring(0, matchStart).lastIndexOfAny(delimiters)
            val startIdx = if (lastDelimBefore != -1) lastDelimBefore + 1 else maxOf(0, matchStart - 20)

            val nextDelimAfterRel = response.substring(matchEnd).indexOfAny(delimiters)
            val endIdx = if (nextDelimAfterRel != -1) matchEnd + nextDelimAfterRel else minOf(response.length, matchEnd + 20)
            val localContext = response.substring(startIdx, endIdx).lowercase()
            
            if (ussdCode == "*222*266#") {
                bonusMb = (bonusMb ?: 0L) + mbValue
            } else if (localContext.contains("bono")) {
                bonusMb = (bonusMb ?: 0L) + mbValue
            } else if (localContext.contains("lte") || localContext.contains("4g")) {
                dataLteMb = (dataLteMb ?: 0L) + mbValue
            } else if (dataMb == null || localContext.contains("paquete") || localContext.contains("todas") || localContext.contains("principal")) {
                dataMb = (dataMb ?: 0L) + mbValue
            }
        }
        
        val genDateMatcher = GENERAL_DATE_REGEX.matcher(response)
        val extractedDate = if (genDateMatcher.find()) genDateMatcher.group(1) else null

        val dayMatcher = DAYS_REGEX.matcher(response)
        var firstDays: Int? = null
        if (dayMatcher.find()) {
            firstDays = dayMatcher.group(1)?.toIntOrNull()
        }

        when (ussdCode) {
            "*222*869#" -> {
                minutesDays = firstDays ?: extractDaysForKeywordSection(response, "min(?:utos)?")
            }
            "*222*767#" -> {
                smsDays = firstDays ?: extractDaysForKeywordSection(response, "sms|mensajes")
            }
            "*222*328#" -> {
                dataDays = firstDays ?: extractDaysForDataSection(response)
            }
            "*222*266#" -> {
                bonusDays = firstDays ?: extractDaysForKeywordSection(response, "bono|bonus")
            }
            "*222*732#" -> {
                val lower = response.lowercase()
                if (lower.contains("puede recargar un monto") || lower.contains("ud puede recargar") || (lower.contains("puede recargar") && !lower.contains("posterior al dia"))) {
                    nextRechargeDays = 0
                    nextRechargeDateStr = "Puede recargar saldo"
                } else if (extractedDate != null) {
                    val (effectiveDateStr, daysRem) = calculateRechargeAvailability(extractedDate)
                    if (daysRem != null && daysRem <= 0) {
                        nextRechargeDays = 0
                        nextRechargeDateStr = "Puede recargar saldo"
                    } else {
                        nextRechargeDateStr = effectiveDateStr
                        if (daysRem != null) nextRechargeDays = daysRem.toInt()
                    }
                } else if (lower.contains("puede recargar")) {
                    nextRechargeDays = 0
                    nextRechargeDateStr = "Puede recargar saldo"
                }
            }
            else -> {
                if (dataDays == null && (dataMb != null || dataLteMb != null || response.contains("datos", ignoreCase = true) || response.contains("paquete", ignoreCase = true) || response.contains("vence", ignoreCase = true))) {
                    dataDays = firstDays
                }
            }
        }

        return ParsedPlanData(
            balanceCup = balance,
            dataMb = dataMb,
            dataLteMb = dataLteMb,
            bonusMb = bonusMb,
            minutesStr = minutesStr,
            sms = sms,
            dataDays = dataDays,
            minutesDays = minutesDays,
            smsDays = smsDays,
            nextRechargeDateStr = nextRechargeDateStr,
            nextRechargeDays = nextRechargeDays,
            bonusDays = bonusDays
        )
    }

    private fun extractDaysForKeywordSection(response: String, keywordRegex: String): Int? {
        val keywordMatcher = Pattern.compile(keywordRegex, Pattern.CASE_INSENSITIVE).matcher(response)
        var bestDistance = Int.MAX_VALUE
        var bestDays: Int? = null
        while (keywordMatcher.find()) {
            val segment = sentenceSegment(response, keywordMatcher.start())
            val dayMatcher = DAYS_REGEX.matcher(segment.text)
            while (dayMatcher.find()) {
                val days = dayMatcher.group(1)?.toIntOrNull() ?: continue
                val absoluteDayPosition = segment.start + dayMatcher.start()
                val distance = abs(absoluteDayPosition - keywordMatcher.start())
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestDays = days
                }
            }
        }
        return bestDays
    }

    private fun extractDaysForDataSection(response: String): Int? {
        val matcher = DATA_MB_REGEX.matcher(response)
        var bestDistance = Int.MAX_VALUE
        var bestDays: Int? = null
        while (matcher.find()) {
            val segment = sentenceSegment(response, matcher.start())
            val lower = segment.text.lowercase()
            if (Regex("""\b(bono|bonus)\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)) continue

            val dayMatcher = DAYS_REGEX.matcher(segment.text)
            while (dayMatcher.find()) {
                val days = dayMatcher.group(1)?.toIntOrNull() ?: continue
                val absoluteDayPosition = segment.start + dayMatcher.start()
                val distance = abs(absoluteDayPosition - matcher.start())
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestDays = days
                }
            }
        }
        return bestDays
    }

    private data class TextSegment(val start: Int, val text: String)

    private fun sentenceSegment(response: String, anchor: Int): TextSegment {
        val delimiters = setOf('.', '!', '?', ';', '\n', '\r')
        var start = anchor
        while (start > 0 && response[start - 1] !in delimiters) start--
        var end = anchor
        while (end < response.length && response[end] !in delimiters) end++
        return TextSegment(start, response.substring(start, end))
    }

    fun calculateRechargeAvailability(dateStr: String?): Pair<String, Long?> {
        if (dateStr.isNullOrBlank()) {
            return Pair("", null)
        }
        try {
            val cleanedDate = dateStr.trim().replace('/', '-').replace('.', '-')
            val parts = cleanedDate.split('-')
            if (parts.size != 3) return Pair(dateStr, null)
            val day = parts[0].toIntOrNull() ?: return Pair(dateStr, null)
            val month = parts[1].toIntOrNull() ?: return Pair(dateStr, null)
            var year = parts[2].toIntOrNull() ?: return Pair(dateStr, null)
            val isTwoDigitYear = parts[2].length == 2
            if (year < 100) {
                year += 2000
            }
            if (day !in 1..31 || month !in 1..12) return Pair(dateStr, null)
            val originalDate = LocalDate.of(year, month, day)
            // ETECSA message specifies recharge can be done after the indicated date (+1 day)
            val allowedRechargeDate = originalDate.plusDays(1)
            val today = LocalDate.now()
            val daysRemaining = ChronoUnit.DAYS.between(today, allowedRechargeDate)

            val sep = if (dateStr.contains('/')) "/" else if (dateStr.contains('.')) "." else "-"
            val formattedDay = String.format(java.util.Locale.US, "%02d", allowedRechargeDate.dayOfMonth)
            val formattedMonth = String.format(java.util.Locale.US, "%02d", allowedRechargeDate.monthValue)
            val formattedYear = if (isTwoDigitYear) {
                String.format(java.util.Locale.US, "%02d", allowedRechargeDate.year % 100)
            } else {
                allowedRechargeDate.year.toString()
            }
            val effectiveDateStr = "$formattedDay$sep$formattedMonth$sep$formattedYear"
            return Pair(effectiveDateStr, daysRemaining)
        } catch (e: Exception) {
            return Pair(dateStr, null)
        }
    }

    fun parseEtecsaSms(smsBody: String): String? {
        val genDateMatcher = GENERAL_DATE_REGEX.matcher(smsBody)
        if (genDateMatcher.find()) {
            return genDateMatcher.group(1)
        }
        return null
    }

    fun evaluateExpiration(dateStr: String?): Triple<Long?, Boolean, Boolean> {
        if (dateStr.isNullOrBlank() || dateStr.equals("Puede recargar saldo", ignoreCase = true)) {
            return Triple(0L, false, false)
        }
        try {
            val cleanedDate = dateStr.trim().replace('/', '-').replace('.', '-')
            val parts = cleanedDate.split('-')
            if (parts.size != 3) return Triple(null, false, false)
            val day = parts[0].toIntOrNull() ?: return Triple(null, false, false)
            val month = parts[1].toIntOrNull() ?: return Triple(null, false, false)
            var year = parts[2].toIntOrNull() ?: return Triple(null, false, false)
            if (year < 100) {
                year += 2000
            }
            val targetDate = LocalDate.of(year, month, day)
            val today = LocalDate.now()
            val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)
            val isNearExp = daysRemaining in 0..1
            val isExpired = daysRemaining < 0
            return Triple(daysRemaining, isNearExp, isExpired)
        } catch (e: Exception) {
            return Triple(null, false, false)
        }
    }
}
