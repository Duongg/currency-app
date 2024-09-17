package domain.model

import androidx.compose.ui.graphics.Color
import fresherColor
import staleColor

enum class RateStatus(
    val title: String,
    val color: Color
) {
    Idle(
        title = "Rates",
        color = Color.White
    ),
    Fresh(
        title = "Fresh Rates",
        color = fresherColor
    ),
    Stale(
        title = "Rates are not fresh",
        color = staleColor
    )
}