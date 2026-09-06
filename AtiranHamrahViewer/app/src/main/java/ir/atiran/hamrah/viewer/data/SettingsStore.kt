package ir.atiran.hamrah.viewer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Everything the app needs to talk to a given Atiran server / database. */
object ConnectionPreset {
    /** Preconfigured company database / session endpoint used on first launch. */
    const val SERVER_IP = "37.143.147.19"
    const val SERVER_PORT = "9595"
    const val SERVER_URL = "http://$SERVER_IP:$SERVER_PORT/Atiran"
    const val DEFAULT_CPU_ID = "00000000-0000"
    const val ADMIN_USER = "AdminAn"
    const val ADMIN_PASS = "St@R2022$"

    // Direct SQL Server / Atiran2
    const val DB_HOST = SERVER_IP
    const val DB_PORT = SERVER_PORT
    const val DB_NAME = "Atiran2"
    const val DB_USER = ADMIN_USER
    const val DB_PASSWORD = ADMIN_PASS
}

data class AtiranSettings(
    val serverUrl: String = ConnectionPreset.SERVER_URL,
    val cpuId: String = ConnectionPreset.DEFAULT_CPU_ID,
    val ownerNum: String = "",
    val inventoryNum: String = "",
    val atiranNum: String = "",
    val carrierNum: String = "",
    val activeLine: String = "",
    val serverAdd: String = "",
    val username: String = ConnectionPreset.ADMIN_USER,
    val password: String = ConnectionPreset.ADMIN_PASS,
    val shMo: String = "",
    val visitorId: String = "",
    val appId: String = "",
    val configured: Boolean = false,
    // Direct database connection
    val dbHost: String = ConnectionPreset.DB_HOST,
    val dbPort: String = ConnectionPreset.DB_PORT,
    val dbName: String = ConnectionPreset.DB_NAME,
    val dbUser: String = ConnectionPreset.DB_USER,
    val dbPassword: String = ConnectionPreset.DB_PASSWORD,
) {
    fun dbSettings(): AtiranDbSettings = AtiranDbSettings(
        host = dbHost.trim(),
        port = dbPort.trim(),
        dbName = dbName.trim(),
        user = dbUser.trim(),
        password = dbPassword,
    )
    fun normalizedServer(): String = serverUrl.trim().trimEnd('/')

    fun toSetInfo(): SetInfo = SetInfo(
        shmo = shMo.ifBlank { null },
        password = password.ifBlank { null },
        _ownerNum = ownerNum.ifBlank { null },
        _InventoryNum = inventoryNum.ifBlank { null },
        _AtiranNum = atiranNum.ifBlank { null },
        _carrierNum = carrierNum.ifBlank { null },
        _ActiveLine = activeLine.ifBlank { null },
        _serverAdd = serverAdd.ifBlank { null },
        VisitorID = visitorId.toIntOrNull(),
    )
}

private val Context.dataStore by preferencesDataStore(name = "atiran_settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val serverUrl = stringPreferencesKey("serverUrl")
        val cpuId = stringPreferencesKey("cpuId")
        val ownerNum = stringPreferencesKey("ownerNum")
        val inventoryNum = stringPreferencesKey("inventoryNum")
        val atiranNum = stringPreferencesKey("atiranNum")
        val carrierNum = stringPreferencesKey("carrierNum")
        val activeLine = stringPreferencesKey("activeLine")
        val serverAdd = stringPreferencesKey("serverAdd")
        val username = stringPreferencesKey("username")
        val password = stringPreferencesKey("password")
        val shMo = stringPreferencesKey("shMo")
        val visitorId = stringPreferencesKey("visitorId")
        val appId = stringPreferencesKey("appId")
        val configured = booleanPreferencesKey("configured")
        val dbHost = stringPreferencesKey("dbHost")
        val dbPort = stringPreferencesKey("dbPort")
        val dbName = stringPreferencesKey("dbName")
        val dbUser = stringPreferencesKey("dbUser")
        val dbPassword = stringPreferencesKey("dbPassword")
    }

    val settings: Flow<AtiranSettings> = context.dataStore.data.map { p ->
        AtiranSettings(
            serverUrl = p[Keys.serverUrl] ?: "",
            cpuId = p[Keys.cpuId] ?: "",
            ownerNum = p[Keys.ownerNum] ?: "",
            inventoryNum = p[Keys.inventoryNum] ?: "",
            atiranNum = p[Keys.atiranNum] ?: "",
            carrierNum = p[Keys.carrierNum] ?: "",
            activeLine = p[Keys.activeLine] ?: "",
            serverAdd = p[Keys.serverAdd] ?: "",
            username = p[Keys.username] ?: "",
            password = p[Keys.password] ?: "",
            shMo = p[Keys.shMo] ?: "",
            visitorId = p[Keys.visitorId] ?: "",
            appId = p[Keys.appId] ?: "",
            configured = p[Keys.configured] ?: false,
            dbHost = p[Keys.dbHost] ?: ConnectionPreset.DB_HOST,
            dbPort = p[Keys.dbPort] ?: ConnectionPreset.DB_PORT,
            dbName = p[Keys.dbName] ?: ConnectionPreset.DB_NAME,
            dbUser = p[Keys.dbUser] ?: ConnectionPreset.DB_USER,
            dbPassword = p[Keys.dbPassword] ?: ConnectionPreset.DB_PASSWORD,
        )
    }

    suspend fun save(s: AtiranSettings, markConfigured: Boolean = true) {
        context.dataStore.edit { p ->
            p[Keys.serverUrl] = s.serverUrl.trim()
            p[Keys.cpuId] = s.cpuId.trim()
            p[Keys.ownerNum] = s.ownerNum.trim()
            p[Keys.inventoryNum] = s.inventoryNum.trim()
            p[Keys.atiranNum] = s.atiranNum.trim()
            p[Keys.carrierNum] = s.carrierNum.trim()
            p[Keys.activeLine] = s.activeLine.trim()
            p[Keys.serverAdd] = s.serverAdd.trim()
            p[Keys.username] = s.username.trim()
            p[Keys.password] = s.password
            p[Keys.shMo] = s.shMo.trim()
            p[Keys.visitorId] = s.visitorId.trim()
            p[Keys.appId] = s.appId.trim()
            p[Keys.configured] = markConfigured
            p[Keys.dbHost] = s.dbHost.trim()
            p[Keys.dbPort] = s.dbPort.trim()
            p[Keys.dbName] = s.dbName.trim()
            p[Keys.dbUser] = s.dbUser.trim()
            p[Keys.dbPassword] = s.dbPassword
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
