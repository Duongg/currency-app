package presentation.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import domain.CurrencyApiService
import domain.MongoRepository
import domain.PreferencesRepository
import domain.model.Currency
import domain.model.RateStatus
import domain.model.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed class HomeUiEvent{
    data object RefreshRate: HomeUiEvent()
    data object SwitchCurrencies: HomeUiEvent()
    data class SaveSourceCurrencyCode(val code: String): HomeUiEvent()
    data class SaveTargetCurrencyCode(val code: String): HomeUiEvent()
    data class AmountChange(val amount: String): HomeUiEvent()
}

class HomeViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val mongoRepository: MongoRepository,
    private val currencyApiService: CurrencyApiService,
): ScreenModel {
    private var _rateStatus: MutableState<RateStatus> = mutableStateOf(RateStatus.Idle)
    val rateStatus: State<RateStatus> = _rateStatus

    private var _sourceCurrency: MutableState<RequestState<Currency>> = mutableStateOf(RequestState.Idle)
    val sourceCurrency: State<RequestState<Currency>> = _sourceCurrency

    private var _targetCurrency: MutableState<RequestState<Currency>> = mutableStateOf(RequestState.Idle)
    val targetCurrency: State<RequestState<Currency>> = _targetCurrency

    private var _allCurrencies = mutableStateListOf<Currency>()
    val allCurrency: List<Currency> = _allCurrencies

    private var _amount = mutableStateOf("")
    val amount: MutableState<String> = _amount
    init {
        screenModelScope.launch {
            fetchNewRates()
            readSourceCurrency()
            readTargetCurrency()
        }
    }

    fun sendEvent(event: HomeUiEvent){
        when(event){
            is HomeUiEvent.RefreshRate -> {
                screenModelScope.launch {
                    fetchNewRates()
                }
            }
            is HomeUiEvent.SwitchCurrencies ->{
                switchCurrencies()
            }
            is HomeUiEvent.SaveSourceCurrencyCode ->{
                saveSourceCurrencyCode(event.code)
            }
            is HomeUiEvent.SaveTargetCurrencyCode ->{
                saveTargetCurrencyCode(event.code)
            }
            is HomeUiEvent.AmountChange ->{
                handleAmountChange(event.amount)
            }
        }
    }

    private fun handleAmountChange(amountInput: String?) {
        val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        val condition = amountInput?.matches(regex) == true && amountInput != _amount.value && amountInput.isNotEmpty()
        if(condition){
            _amount.value = amountInput.toString()
        }
    }

    private fun saveSourceCurrencyCode(code: String){
        screenModelScope.launch(Dispatchers.IO) {
            preferencesRepository.saveSourceCurrencyCode(code)
        }
    }

    private fun saveTargetCurrencyCode(code: String){
        screenModelScope.launch(Dispatchers.IO) {
            preferencesRepository.saveTargetCurrencyCode(code)
        }
    }

    private fun switchCurrencies() {
        val source = _sourceCurrency.value
        val target = _targetCurrency.value
        _sourceCurrency.value = target
        _targetCurrency.value = source
    }

    private fun readSourceCurrency(){
        screenModelScope.launch(Dispatchers.Main){
            preferencesRepository.readSourceCurrencyCode().collectLatest { currencyCode ->
                val selectedCurrency = _allCurrencies.find { it.code == currencyCode.name }
                if(selectedCurrency != null){
                    _sourceCurrency.value = RequestState.Success(data = selectedCurrency)
                }else{
                    _sourceCurrency.value = RequestState.Error(message = "Could not find the selected currency")
                }
            }
        }
    }

    private fun readTargetCurrency(){
        screenModelScope.launch(Dispatchers.Main){
            preferencesRepository.readTargetCurrencyCode().collectLatest { currencyCode ->
                val selectedCurrency = _allCurrencies.find { it.code == currencyCode.name }
                if(selectedCurrency != null){
                    _targetCurrency.value = RequestState.Success(data = selectedCurrency)
                }else{
                    _targetCurrency.value = RequestState.Error(message = "Could not find the selected currency")
                }
            }
        }
    }

    private suspend fun fetchNewRates(){
        try {
            val localCache = mongoRepository.readCurrencyData().first()
            if(localCache.isSuccess()){
                if(localCache.getSuccessData().isNotEmpty()){
                    println("HomeViewModel: DATABASE IS FULL")
                    _allCurrencies.clear()
                    _allCurrencies.addAll(localCache.getSuccessData())
                    if(!preferencesRepository.isDataFresh(Clock.System.now().toEpochMilliseconds())){
                        println("HomeViewModel: DATABASE IS NOT FRESH")
                        cacheData()
                    }else{
                        println("HomeViewModel: DATABASE IS FRESH")
                    }
                }else{
                    println("HomeViewModel: DATABASE NEEDS DATA")
                    cacheData()
                }
            }else if(localCache.isError()){
                println("HomeViewModel: ERROR READING LOCAL DATABASE ${localCache.getErrorMessage()}")
            }

            getRateStatus()
        }catch (e: Exception){
            println(e.message)
        }
    }
    private suspend fun cacheData(){
        val fetchedData = currencyApiService.getLatestExchangeRates()
        if(fetchedData.isSuccess()){
            mongoRepository.cleanUp()
            fetchedData.getSuccessData().forEach {
                println("HomeViewModel: ADDING ${it.code}")
                mongoRepository.insertCurrencyData(it)
                println("HomeViewModel: UPDATING _allCurrencies")
                _allCurrencies.clear()
                _allCurrencies.addAll(fetchedData.getSuccessData())
            }
        }else if(fetchedData.isError()){
            println("HomeViewModel: FETCHING FAILED ${fetchedData.getErrorMessage()}")
        }
    }
    private suspend fun getRateStatus(){
        _rateStatus.value = if(preferencesRepository.isDataFresh(
            currentTimestamp = Clock.System.now().toEpochMilliseconds()
        )) RateStatus.Fresh
        else RateStatus.Stale
    }
}