package com.ams.megascu.data.ussd

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

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
        "(\\d{2}[-/.][\\d]{2}[-/.][\\d]{2,4})",
        Pattern.CASE_INSENSITIVE
    )
    
    private val MINUTES_REGEX = Pattern.compile(
        "([0-9:]+)\\s*(?:MIN|minutos?|mins?\\.?|min\\.?)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val SMS_REGEX = Pattern.compile(
        "([0-9]+)\\s*(?:SMS|sms|mensajes?|msgs?\\.?)",
        Pattern.CASE_INSENSITIVE
    )

    private val CUP_BALANCE_REGEX = Pattern.compile(
        "(?:(?:Su\\s+)?Saldo(?:\\s+principal|\\s+actual|\\s+disponible)?|CUP|saldo)(?:\\s+(?:es\\s+de|es))?[:\\s]*([0-9]+(?:[.,][0-9]+)?)\\s*(?:CUP)?",
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
            val mbValue = if (unit == "GB") (rawValue * 1024).toLong() else rawValue.toLong()
            
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
        
        val daysMatcher = DAYS_REGEX.matcher(response)
        val extractedDays = if (daysMatcher.find()) daysMatcher.group(1)?.toIntOrNull() else null

        val genDateMatcher = GENERAL_DATE_REGEX.matcher(response)
        val extractedDate = if (genDateMatcher.find()) genDateMatcher.group(1) else null

        when (ussdCode) {
            "*222*869#" -> {
                minutesDays = extractedDays
                if (minutesStr == null) minutesStr = "0"
            }
            "*222*767#" -> {
                smsDays = extractedDays
                if (sms == null) sms = 0
            }
            "*222*328#" -> {
                dataDays = extractedDays
                if (dataMb == null) dataMb = 0L
                if (dataLteMb == null) dataLteMb = 0L
            }
            "*222*266#" -> {
                bonusDays = extractedDays
                if (bonusMb == null) bonusMb = 0L
            }
            "*222*732#" -> {
                if (extractedDate != null) {
                    val (effectiveDateStr, daysRem) = calculateRechargeAvailability(extractedDate)
                    nextRechargeDateStr = effectiveDateStr
                    if (daysRem != null) {
                        nextRechargeDays = daysRem.toInt()
                    }
                }
            }
        }

        if (dataDays == null && extractedDays != null && ussdCode != "*222*869#" && ussdCode != "*222*767#" && ussdCode != "*222*266#") {
            dataDays = extractedDays
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
        if (dateStr.isNullOrBlank()) {
            return Triple(null, false, false)
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
