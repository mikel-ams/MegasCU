package com.ams.megascu.data.ussd

/** One final outcome per requested code. A successful retry replaces its earlier failure. */
class UssdBatchReport {
    private val outcomes = linkedMapOf<String, UssdResult>()

    fun record(code: String, result: UssdResult) { outcomes[code] = result }
    val total: Int get() = outcomes.size
    val successful: Int get() = outcomes.values.count { it is UssdResult.Success }
    val failures: Map<String, UssdResult.Error>
        get() = outcomes.mapNotNull { (code, result) ->
            (result as? UssdResult.Error)?.let { code to it }
        }.toMap()
    val isComplete: Boolean get() = total > 0 && failures.isEmpty()
    fun failedCodes(): List<String> = failures.keys.toList()

    fun message(simSlot: Int): String = when {
        total == 0 -> "No se ejecutó ninguna consulta en la SIM $simSlot."
        isComplete -> "SIM $simSlot: actualización completa ($successful/$total consultas)."
        successful == 0 -> "SIM $simSlot: no se pudo actualizar. Fallaron las $total consultas. Inténtalo de nuevo."
        else -> "SIM $simSlot: actualización parcial ($successful/$total). No se completó: " +
            failures.keys.joinToString(", ") { labelForCode(it) } + ". Inténtalo de nuevo."
    }

    companion object {
        val STATUS_CODES = listOf("*222#", "*222*328#", "*222*266#", "*222*869#", "*222*767#", "*222*732#")
        fun labelForCode(code: String): String = when (code) {
            "*222#" -> "saldo"
            "*222*328#" -> "datos"
            "*222*266#" -> "bonos"
            "*222*869#" -> "minutos"
            "*222*767#" -> "SMS"
            "*222*732#" -> "recarga"
            else -> code
        }
    }
}
