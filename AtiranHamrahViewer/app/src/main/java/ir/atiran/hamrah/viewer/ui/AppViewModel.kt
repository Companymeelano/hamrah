package ir.atiran.hamrah.viewer.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.atiran.hamrah.viewer.data.AtiranClient
import ir.atiran.hamrah.viewer.data.AtiranRepository
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Loading : Screen()
    data object Settings : Screen()
    data object Login : Screen()
    data object Home : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)

    val settings: StateFlow<AtiranSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AtiranSettings())

    var screen by mutableStateOf<Screen>(Screen.Loading)
        private set

    var busy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            settings.collect { s ->
                screen = when {
                    s.configured -> Screen.Login
                    else -> Screen.Settings
                }
            }
        }
    }

    private var cachedRepo: AtiranRepository? = null
    private var repoCacheKey: String? = null

    /** Stable repository instance while server/CPUID unchanged. */
    fun repository(): AtiranRepository? {
        val s = settings.value
        val key = s.serverUrl.trim().trimEnd('/') + "|" + s.cpuId.trim()
        if (cachedRepo == null || repoCacheKey != key) {
            repoCacheKey = key
            cachedRepo = if (s.serverUrl.isBlank() || s.cpuId.isBlank()) {
                null
            } else {
                AtiranRepository(AtiranClient(s))
            }
        }
        return cachedRepo
    }

    fun saveSettings(s: AtiranSettings) {
        viewModelScope.launch {
            busy = true
            error = null
            try {
                store.save(s)
                screen = Screen.Login
            } catch (e: Exception) {
                error = e.message ?: "خطا در ذخیره تنظیمات"
            } finally {
                busy = false
            }
        }
    }

    fun goSettings() {
        screen = Screen.Settings
    }

    fun goLogin() {
        screen = Screen.Login
    }

    fun goHome() {
        screen = Screen.Home
    }

    fun logout() {
        viewModelScope.launch {
            store.clear()
            error = null
            screen = Screen.Settings
        }
    }
}
