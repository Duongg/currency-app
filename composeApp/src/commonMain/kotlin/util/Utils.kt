package util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import currencyapp.composeapp.generated.resources.Res
import currencyapp.composeapp.generated.resources.bebas_nue_regular
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.Font


fun calculateExchangeRate(source: Double, target: Double): Double{
    return target/source
}

fun convert(amount: Double, exchangeRate: Double): Double {
    return amount * exchangeRate
}

fun displayCurrentDateTime(): String {
    val currentTimeStamp = Clock.System.now()
    val date = currentTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault())

    val dayOfMonth = date.dayOfMonth
    val month = date.month.toString().lowercase().replaceFirstChar { if(it.isLowerCase()) it.titlecase() else it.toString() }
    val year = date.year

    val suffix = when {
        dayOfMonth in 11..13 -> "th"
        dayOfMonth % 10 == 1 -> "st"
        dayOfMonth % 10 == 2 -> "nd"
        dayOfMonth % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$dayOfMonth$suffix $month, $year."
}

@Composable
fun getBebasFontFamily()  = FontFamily(Font(Res.font.bebas_nue_regular))