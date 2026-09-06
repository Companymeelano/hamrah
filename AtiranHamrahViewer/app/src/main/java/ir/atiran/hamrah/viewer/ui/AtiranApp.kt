package ir.atiran.hamrah.viewer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.atiran.hamrah.viewer.ui.screens.HomeScreen
import ir.atiran.hamrah.viewer.ui.screens.LandingScreen
import ir.atiran.hamrah.viewer.ui.screens.LoginScreen
import ir.atiran.hamrah.viewer.ui.screens.SettingsScreen

@Composable
fun AtiranApp(vm: AppViewModel) {
    val settings by vm.settings.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (vm.screen) {
            is Screen.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is Screen.Landing -> LandingScreen(vm = vm)
            is Screen.Settings -> SettingsScreen(vm = vm, initial = settings)
            is Screen.Login -> LoginScreen(vm = vm, settings = settings)
            is Screen.Home -> HomeScreen(vm = vm, settings = settings)
        }
    }
}
