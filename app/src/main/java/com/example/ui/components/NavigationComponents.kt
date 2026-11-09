package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.model.AppScreen
import com.example.ui.theme.*

fun getScreenIcon(screen: AppScreen): ImageVector {
    return when (screen) {
        AppScreen.HOME -> Icons.Default.Home
        AppScreen.DASHBOARD -> Icons.Default.Dashboard
        AppScreen.CHAT -> Icons.Default.ChatBubble
        AppScreen.IMAGE_STUDIO -> Icons.Default.AutoFixHigh
        AppScreen.VIDEO_STUDIO -> Icons.Default.Videocam
        AppScreen.VOICE_STUDIO -> Icons.Default.RecordVoiceOver
        AppScreen.TEXT_TO_SPEECH -> Icons.Default.VolumeUp
        AppScreen.SPEECH_TO_TEXT -> Icons.Default.Hearing
        AppScreen.MODELS_DIRECTORY -> Icons.Default.GridView
        AppScreen.HISTORY -> Icons.Default.History
        AppScreen.PRICING -> Icons.Default.Loyalty
        AppScreen.TRANSACTIONS -> Icons.Default.ReceiptLong
        AppScreen.PROFILE -> Icons.Default.AccountCircle
        AppScreen.SETTINGS -> Icons.Default.Settings
        AppScreen.HELP -> Icons.Default.HelpOutline
        AppScreen.ADMIN -> Icons.Default.AdminPanelSettings
        AppScreen.AUTH -> Icons.Default.Lock
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    videoCredits: Long,
    onMenuClick: () -> Unit,
    onCreditClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Futuristic Chrome Avatar with Cyan Turquoise Glow & Gold Neural Spark
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ChromeMetalLight, ChromeMetalDark))
                            )
                            .border(1.5.dp, CyanTurquoise, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.aura_app_icon_1793994962108),
                            contentDescription = "آیکون ORION AI",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Golden Neural Spark indicator dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeuralGoldSpark)
                            .border(0.5.dp, Color.White, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ORION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = OutfitFontFamily,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = OutfitFontFamily,
                            color = CyanTurquoise
                        )
                    }
                    Text(
                        text = currentScreen.titleFa,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanTurquoise
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "منو",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            VideoCreditPill(
                credits = videoCredits,
                onClick = onCreditClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("profile_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevation2)
                        .border(1.dp, TranslucentBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "حساب",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VoidCanvas.copy(alpha = 0.95f),
            titleContentColor = TextPrimary
        )
    )
}

@Composable
fun AppBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    val mainTabs = listOf(
        AppScreen.HOME,
        AppScreen.CHAT,
        AppScreen.VIDEO_STUDIO,
        AppScreen.MODELS_DIRECTORY,
        AppScreen.PRICING
    )

    NavigationBar(
        containerColor = SurfaceElevation1.copy(alpha = 0.95f),
        contentColor = TextSecondary,
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .border(1.dp, TranslucentBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        mainTabs.forEach { screen ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = getScreenIcon(screen),
                        contentDescription = screen.titleFa,
                        tint = if (selected) ElectricIndigo else TextTertiary
                    )
                },
                label = {
                    Text(
                        text = screen.titleFa,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) ElectricIndigo else TextTertiary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ElectricIndigo.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_${screen.name}")
            )
        }
    }
}

@Composable
fun DrawerContent(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerContainerColor = SurfaceElevation1,
        drawerContentColor = TextPrimary,
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Drawer Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ChromeMetalLight, ChromeMetalDark))
                            )
                            .border(2.dp, CyanTurquoise, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.aura_app_icon_1793994962108),
                            contentDescription = "ORION AI",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NeuralGoldSpark)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ORION AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = OutfitFontFamily,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanTurquoise.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, CyanTurquoise)
                        ) {
                            Text(
                                text = "CORE v4.2",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = CyanTurquoise,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "پلتفرم هوش مصنوعی و پردازش عصبی",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = TranslucentBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Sections
            val categories = listOf("اصلی", "استودیو", "اکتشاف", "مالی", "کاربری", "سیستم", "مدیریت", "پشتیبانی")

            categories.forEach { category ->
                val screensInCategory = AppScreen.values().filter { it.category == category }
                if (screensInCategory.isNotEmpty()) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                    )
                    screensInCategory.forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = getScreenIcon(screen),
                                    contentDescription = null,
                                    tint = if (selected) ElectricIndigo else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.titleFa,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selected) TextPrimary else TextSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                onSelectScreen(screen)
                                onCloseDrawer()
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ElectricIndigo.copy(alpha = 0.15f),
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("drawer_item_${screen.name}")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AppDesktopSidebar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(SurfaceElevation1)
            .border(1.dp, TranslucentBorder)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Brand logo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(ChromeMetalLight, ChromeMetalDark))
                        )
                        .border(1.5.dp, CyanTurquoise, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aura_app_icon_1793994962108),
                        contentDescription = "ORION AI",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NeuralGoldSpark)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ORION AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontFamily = OutfitFontFamily,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "پلتفرم هوش مصنوعی کوانتومی",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = TranslucentBorder)
        Spacer(modifier = Modifier.height(12.dp))

        AppScreen.values().forEach { screen ->
            val selected = currentScreen == screen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onNavigate(screen) },
                color = if (selected) ElectricIndigo.copy(alpha = 0.18f) else Color.Transparent,
                border = if (selected) BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.5f)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getScreenIcon(screen),
                        contentDescription = null,
                        tint = if (selected) ElectricIndigo else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = screen.titleFa,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) TextPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
