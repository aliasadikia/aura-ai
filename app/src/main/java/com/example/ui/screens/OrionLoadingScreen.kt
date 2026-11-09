package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OrionLoadingScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0.15f) }
    var statusText by remember { mutableStateOf("در حال مقداردهی اولیه هسته زرهی ORION...") }

    // Pulsing Animation for Cyan Luminous Visor Glow
    val infiniteTransition = rememberInfiniteTransition(label = "orion_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val sparkRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spark_rotation"
    )

    LaunchedEffect(Unit) {
        delay(400)
        progress = 0.40f
        statusText = "همگام‌سازی مسیرهای عصبی و ویزور فیروزه‌ای..."
        delay(500)
        progress = 0.75f
        statusText = "تثبیت فرکانس کلاستر پردازشی ORION AI..."
        delay(500)
        progress = 1.0f
        statusText = "آماده‌سازی رابط کاربری زرهی • خوش آمدید"
        delay(400)
        onLoadingFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SurfaceElevation2,
                        SurfaceElevation1,
                        VoidCanvas
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Metallic Chrome Cyber Armor Frame with Cyan Halo
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer Cyan Energy Halo
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanTurquoise.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Chrome Armor Border Ring
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ChromeMetalLight, ChromeMetalDark, ChromeMetalMid)
                            )
                        )
                        .border(
                            BorderStroke(
                                2.dp,
                                Brush.linearGradient(listOf(CyanTurquoise, NeuralGoldSpark))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Robot Avatar Image
                    Image(
                        painter = painterResource(id = R.drawable.aura_app_icon_1793994962108),
                        contentDescription = "آیکون ربات زرهی ORION AI",
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Rotating Neural Golden Spark on Top/Forehead
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-16).dp, y = (10).dp)
                        .size(32.dp)
                        .rotate(sparkRotation)
                        .clip(CircleShape)
                        .background(NeuralGoldSpark.copy(alpha = 0.25f))
                        .border(1.dp, NeuralGoldSpark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "جرقه عصبی طلایی",
                        tint = NeuralGoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Branding Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ORION",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontFamily = OutfitFontFamily,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanTurquoise.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyanTurquoise.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = OutfitFontFamily,
                        color = CyanTurquoise,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Persian Tagline
            Text(
                text = "پلتفرم جامع هوش مصنوعی و پردازش عصبی پیشرفته",
                style = MaterialTheme.typography.bodyMedium,
                color = ChromeMetalLight,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Cybernetic Armor Progress Bar Container
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceElevation2,
                border = BorderStroke(1.dp, TranslucentBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = CyanTurquoise,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "کلاستر زرهی فعال",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeuralGoldSpark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanTurquoise,
                        trackColor = SurfaceElevation3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = ChromeMetalLight,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick bypass button for instant access
            TextButton(
                onClick = onLoadingFinished,
                modifier = Modifier.testTag("skip_loading_button")
            ) {
                Text(
                    text = "ورود مستقیم به سیستم",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )
            }
        }
    }
}
