package com.ams.megascu.data.ussd

/** A nonempty modem reply is not necessarily a balance response. Unknown status
 * replies must not update timestamps or turn a failed batch into apparent success.
 */
object UssdStatusResponseValidator {
    fun parse(response: String, code: String): ParsedPlanData? {
        if (response.isBlank()) return null
        val parsed = EtecsaUssdParser.parseUssdResponse(response, code)
        val recognized = when (code) {
            "*222#" -> parsed.balanceCup != null
            "*222*328#" -> parsed.dataMb != null || parsed.dataLteMb != null
            "*222*266#" -> parsed.bonusMb != null
            "*222*869#" -> parsed.minutesStr != null
            "*222*767#" -> parsed.sms != null
            "*222*732#" -> parsed.nextRechargeDateStr != null || parsed.nextRechargeDays != null
            else -> true // Keep interactive menus and other existing queries compatible.
        }
        if (recognized) return parsed
        // An explicit absence is a legitimate zero, not a network failure.
        val absence = Regex("""\bno\s+(?:tiene|dispone\s+de|cuenta\s+con|posee)\s+(?:ning[uú]n(?:os|as|a)?\s+)?(?:paquetes?|planes?|bonos?|datos|minutos?|sms|mensajes?)\b""", RegexOption.IGNORE_CASE)
        val subject = absence.find(response)?.value?.lowercase() ?: return null
        return when {
            code == "*222*328#" && ("paquete" in subject || "plan" in subject || "datos" in subject) ->
                parsed.copy(dataMb = 0, dataLteMb = 0, dataDays = 0)
            code == "*222*266#" && "bono" in subject -> parsed.copy(bonusMb = 0, bonusDays = 0)
            code == "*222*869#" && ("minuto" in subject || "plan" in subject) -> parsed.copy(minutesStr = "0", minutesDays = 0)
            code == "*222*767#" && ("sms" in subject || "mensaje" in subject || "plan" in subject) -> parsed.copy(sms = 0, smsDays = 0)
            else -> null
        }
    }
}
