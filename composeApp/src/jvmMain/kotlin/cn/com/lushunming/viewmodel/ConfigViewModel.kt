package cn.com.lushunming.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.com.lushunming.models.AppConfig
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.util.HttpClientUtil.setProxy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ConfigViewModel(private val service: ConfigService) : ViewModel() {
    private val _config = MutableStateFlow<AppConfig>(AppConfig(1, "", 0))
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    init {
        getConfig()
    }

    private fun getConfig() {
        viewModelScope.launch {
            _config.value = service.getConfig() ?: AppConfig(1, "", 0)
        }
    }

    fun saveConfig(config: AppConfig) {
        viewModelScope.launch {
            if (config.open == 1) {
                setProxy(config.proxy)
            } else {
                setProxy(null)
            }
            service.saveConfig(config)

        }
    }


}