package com.milehighweb.riftclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.milehighweb.riftclash.ui.screens.GameScreen
import com.milehighweb.riftclash.ui.screens.MainMenuScreen
import com.milehighweb.riftclash.ui.theme.RiftClashTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RiftClashTheme {
                var showGame by remember { mutableStateOf(false) }
                val snapshot by viewModel.snapshot.collectAsState()

                if (showGame) {
                    GameScreen(
                        viewModel = viewModel,
                        snapshot = snapshot,
                        onExitToMenu = { showGame = false },
                    )
                } else {
                    MainMenuScreen(
                        onStartGame = {
                            viewModel.startNewGame()
                            showGame = true
                        },
                    )
                }
            }
        }
    }
}
