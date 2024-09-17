package presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import domain.model.CurrencyType
import presentation.component.CurrencyPickerDialog
import presentation.component.HomeBody
import presentation.component.HomeHeader
import surfaceColor

class HomeScreen: Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<HomeViewModel>()
        val allCurrencies = viewModel.allCurrency
        val rateStatus by viewModel.rateStatus
        val sourceCurrency by viewModel.sourceCurrency
        val targetCurrency by viewModel.targetCurrency
        val amount by viewModel.amount

        var selectedCurrencyType: CurrencyType by remember { mutableStateOf(CurrencyType.None) }
        var dialogOpened by remember { mutableStateOf(false) }
        if (dialogOpened && selectedCurrencyType != CurrencyType.None) {
            CurrencyPickerDialog(
                currencies = allCurrencies,
                currencyType = selectedCurrencyType,
                onConfirmClick = {currencyCode ->
                    if(selectedCurrencyType is CurrencyType.Source){
                        viewModel.sendEvent(
                            HomeUiEvent.SaveSourceCurrencyCode(code = currencyCode.name)
                        )
                    }else if(selectedCurrencyType is CurrencyType.Target){
                        viewModel.sendEvent(
                            HomeUiEvent.SaveTargetCurrencyCode(code = currencyCode.name)
                        )
                    }
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                },
                onDismiss = {
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                }
            )
        }
       Column(modifier = Modifier.fillMaxSize().background(surfaceColor)){
           HomeHeader(
               status = rateStatus,
               source = sourceCurrency,
               target = targetCurrency,
               amount = amount,
               onAmountChange = { it ->
                  viewModel.sendEvent(HomeUiEvent.AmountChange(amount = it))
               },
               onRatesRefresh = {viewModel.sendEvent(HomeUiEvent.RefreshRate)},
               onSwitchClick = {viewModel.sendEvent(HomeUiEvent.SwitchCurrencies)},
               onCurrencyTypeSelect = {currencyType ->
                   selectedCurrencyType = currencyType
                   dialogOpened = true
               }
           )
           HomeBody(
               source = sourceCurrency,
               target = targetCurrency,
               amount = amount
           )
       }
    }
}