package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.PlatformRepository
import com.example.model.AppScreen
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidCanvas
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var isAppLoading by remember { mutableStateOf(true) }
                    if (isAppLoading) {
                        OrionLoadingScreen(onLoadingFinished = { isAppLoading = false })
                    } else {
                        OrionMainApp(onRequestReload = { isAppLoading = true })
                    }
                }
            }
        }
    }
}

@Composable
fun OrionMainApp(onRequestReload: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val videoCredits by PlatformRepository.videoCredits.collectAsState()

    // Handle back button: return to Home if on another screen
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentScreen = AppScreen.HOME
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidCanvas)
    ) {
        val isExpandedScreen = maxWidth >= 840.dp

        if (isExpandedScreen) {
            // DESKTOP & TABLET WIDE LAYOUT: Permanent Sidebar + Header + Content
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar on the Right (in RTL mode)
                AppDesktopSidebar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )

                // Main Content Area
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = {
                        AppTopBar(
                            currentScreen = currentScreen,
                            videoCredits = videoCredits,
                            onMenuClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            onCreditClick = { currentScreen = AppScreen.PRICING },
                            onProfileClick = { currentScreen = AppScreen.PROFILE }
                        )
                    },
                    containerColor = VoidCanvas
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppScreenContent(
                            screen = currentScreen,
                            onNavigate = { currentScreen = it }
                        )
                    }
                }
            }
        } else {
            // MOBILE COMPACT LAYOUT: Modal Navigation Drawer + Bottom Navigation Bar
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        currentScreen = currentScreen,
                        onSelectScreen = { currentScreen = it },
                        onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                    )
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar(
                            currentScreen = currentScreen,
                            videoCredits = videoCredits,
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onCreditClick = { currentScreen = AppScreen.PRICING },
                            onProfileClick = { currentScreen = AppScreen.PROFILE }
                        )
                    },
                    bottomBar = {
                        AppBottomNavBar(
                            currentScreen = currentScreen,
                            onNavigate = { currentScreen = it }
                        )
                    },
                    containerColor = VoidCanvas
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppScreenContent(
                            screen = currentScreen,
                            onNavigate = { currentScreen = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppScreenContent(
    screen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    when (screen) {
        AppScreen.HOME -> HomeScreen(onNavigate = onNavigate)
        AppScreen.DASHBOARD -> DashboardScreen(onNavigate = onNavigate)
        AppScreen.CHAT -> ChatScreen()
        AppScreen.IMAGE_STUDIO -> ImageStudioScreen()
        AppScreen.VIDEO_STUDIO -> VideoStudioScreen(onNavigateToPricing = { onNavigate(AppScreen.PRICING) })
        AppScreen.VOICE_STUDIO -> VoiceStudioScreen(
            onNavigateToStt = { onNavigate(AppScreen.SPEECH_TO_TEXT) },
            onNavigateToTts = { onNavigate(AppScreen.TEXT_TO_SPEECH) }
        )
        AppScreen.TEXT_TO_SPEECH -> TextToSpeechScreen()
        AppScreen.SPEECH_TO_TEXT -> SpeechToTextScreen(onNavigateToTts = { onNavigate(AppScreen.TEXT_TO_SPEECH) })
        AppScreen.MODELS_DIRECTORY -> ModelsDirectoryScreen(
            onNavigateToChat = { onNavigate(AppScreen.CHAT) },
            onNavigateToVideo = { onNavigate(AppScreen.VIDEO_STUDIO) }
        )
        AppScreen.HISTORY -> HistoryScreen()
        AppScreen.PRICING -> PricingScreen(onNavigateToTransactions = { onNavigate(AppScreen.TRANSACTIONS) })
        AppScreen.TRANSACTIONS -> TransactionsScreen()
        AppScreen.PROFILE -> ProfileScreen(onNavigateToPricing = { onNavigate(AppScreen.PRICING) })
        AppScreen.SETTINGS -> SettingsScreen()
        AppScreen.HELP -> HelpScreen()
        AppScreen.ADMIN -> AdminScreen()
        AppScreen.AUTH -> AuthScreen(onLoginSuccess = { onNavigate(AppScreen.HOME) })
    }
}
