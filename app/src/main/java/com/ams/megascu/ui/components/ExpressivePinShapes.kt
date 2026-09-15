package com.ams.megascu.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class M3ExpressiveShapeDef(
    val name: String,
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp
)

object ExpressivePinShapes {
    val pool = listOf(
        M3ExpressiveShapeDef("Square", 5.dp, 5.dp, 5.dp, 5.dp),
        M3ExpressiveShapeDef("Arch", 14.dp, 14.dp, 2.dp, 2.dp),
        M3ExpressiveShapeDef("Semicircle", 14.dp, 14.dp, 0.dp, 0.dp),
        M3ExpressiveShapeDef("Fan", 14.dp, 2.dp, 2.dp, 2.dp),
        M3ExpressiveShapeDef("Diamond", 14.dp, 2.dp, 14.dp, 2.dp),
        M3ExpressiveShapeDef("4-sided cookie", 14.dp, 4.dp, 4.dp, 14.dp),
        M3ExpressiveShapeDef("4-sided cookie inverted", 4.dp, 14.dp, 14.dp, 4.dp),
        M3ExpressiveShapeDef("Ghost-ish", 14.dp, 14.dp, 8.dp, 8.dp),
        M3ExpressiveShapeDef("Triangle", 14.dp, 14.dp, 3.dp, 3.dp),
        M3ExpressiveShapeDef("Gem", 12.dp, 12.dp, 4.dp, 4.dp),
        M3ExpressiveShapeDef("Arrow", 14.dp, 14.dp, 7.dp, 7.dp),
        M3ExpressiveShapeDef("Pill", 14.dp, 14.dp, 14.dp, 14.dp)
    )

    fun get4UniqueRandomShapes(): List<M3ExpressiveShapeDef> {
        return pool.shuffled().take(4)
    }
}
