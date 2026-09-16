package com.milehighweb.riftclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.milehighweb.riftclash.ui.screens.CollectionScreen
import com.milehighweb.riftclash.ui.screens.CrashReportScreen
import com.milehighweb.riftclash.ui.screens.GameScreen
import com.milehighweb.riftclash.ui.screens.MainMenuScreen
import com.milehighweb.riftclash.ui.screens.RulesScreen
import com.milehighweb.riftclash.ui.theme.RiftClashTheme

private enum class Screen { MENU, GAME, RULES, COLLECTION }

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw behind the system bars consistently across API levels (Android 15+ enforces this
        // regardless), so every screen's own safeDrawingPadding() is what actually keeps content
        // clear of the status bar, a display cutout, or a nav bar rendered along a landscape edge.
        enableEdgeToEdge()
        val lastCrash = CrashReporter.consumeLastCrash(application)
        setContent {
            RiftClashTheme {
                var screen by remember { mutableStateOf(Screen.MENU) }
                var crashReport by remember { mutableStateOf(lastCrash) }
                val snapshot by viewModel.snapshot.collectAsState()

                val currentCrashReport = crashReport
                when {
                    currentCrashReport != null -> CrashReportScreen(
                        report = currentCrashReport,
                        onDismiss = { crashReport = null },
                    )
                    else -> when (screen) {
                        Screen.MENU -> MainMenuScreen(
                            onStartGame = {
                                viewModel.startNewGame()
                                screen = Screen.GAME
                            },
                            onShowRules = { screen = Screen.RULES },
                            onShowCollection = { screen = Screen.COLLECTION },
                        )
                        Screen.GAME -> GameScreen(
                            viewModel = viewModel,
                            snapshot = snapshot,
                            onExitToMenu = { screen = Screen.MENU },
                        )
                        Screen.RULES -> RulesScreen(onBack = { screen = Screen.MENU })
                        Screen.COLLECTION -> CollectionScreen(onBack = { screen = Screen.MENU })
                    }
                }
            }
        }
    }
}
