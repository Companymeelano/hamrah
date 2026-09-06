package ir.atiran.hamrah.viewer.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.atiran.hamrah.viewer.data.AtiranClient
import ir.atiran.hamrah.viewer.data.AtiranDbRepository
import ir.atiran.hamrah.viewer.data.AtiranRepository
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Loading : Screen()
    data object Landing : Screen()
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
        screen = Screen.Landing
    }

    private var cachedRepo: AtiranRepository? = null
    private var repoCacheKey: String? = null
    private var dbRepo: AtiranDbRepository? = null
    private var dbRepoKey: String? = null

    /** Stable direct-database repository while host/port/database/user unchanged. */
    fun dbRepository(): AtiranDbRepository {
        val s = settings.value
        val key = listOf(s.dbHost, s.dbPort, s.dbName, s.dbUser).joinToString("|")
        if (dbRepo == null || dbRepoKey != key) {
            dbRepoKey = key
            dbRepo = AtiranDbRepository(s.dbSettings())
        }
        return dbRepo!!
    }

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

    /** Save embedded preset, verify direct DB connection, then open reports. */
    fun connectWithPreset() {
        viewModelScope.launch {
            busy = true
            error = null
            try {
                val preset = AtiranSettings()
                store.save(preset)
                val info = dbRepository().testConnection()
                store.save(settings.value.copy(configured = true))
                error = "متصل شد: $info"
                screen = Screen.Home
            } catch (e: Exception) {
                error = "اتصال مستقیم به دیتابیس ناموفق: ${e.message}"
                screen = Screen.Login
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
            screen = Screen.Landing
        }
    }
}
