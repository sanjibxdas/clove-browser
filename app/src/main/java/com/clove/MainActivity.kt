package com.clove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clove.browser.BrowserViewModel
import com.clove.ui.BrowserScreen
import com.clove.ui.theme.CloveTheme
import com.clove.ui.theme.LocalSafariPalette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: BrowserViewModel = viewModel()

            CloveTheme(private = vm.isPrivateMode, darkTheme = isSystemInDarkTheme()) {
                val palette = LocalSafariPalette.current
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
                        val controller = WindowCompat.getInsetsController(window, view)
                        // Dark status/nav icons on light chrome; light icons in private (dark) mode.
                        controller.isAppearanceLightStatusBars = !palette.isDark
                        controller.isAppearanceLightNavigationBars = !palette.isDark
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = palette.pageBackground,
                ) {
                    BrowserScreen(viewModel = vm)
                }
            }
        }
    }
}
