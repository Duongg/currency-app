@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package currencyapp.composeapp.generated.resources

import kotlin.OptIn
import org.jetbrains.compose.resources.FontResource

private object CommonMainFont0 {
  public val bebas_nue_regular: FontResource by 
      lazy { init_bebas_nue_regular() }
}

internal val Res.font.bebas_nue_regular: FontResource
  get() = CommonMainFont0.bebas_nue_regular

private fun init_bebas_nue_regular(): FontResource = org.jetbrains.compose.resources.FontResource(
  "font:bebas_nue_regular",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/currencyapp.composeapp.generated.resources/font/bebas_nue_regular.ttf", -1, -1),
    )
)
