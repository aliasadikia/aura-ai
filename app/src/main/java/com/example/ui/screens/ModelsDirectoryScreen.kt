package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlatformRepository
import com.example.model.AIModelInfo
import com.example.model.AppScreen
import com.example.model.ModelCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ModelsDirectoryScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToVideo: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ModelCategory.ALL) }
    var selectedModelDialog by remember { mutableStateOf<AIModelInfo?>(null) }

    val filteredModels = PlatformRepository.aiModelsDirectory.filter { model ->
        val matchesCategory = selectedCategory == ModelCategory.ALL || model.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                model.name.contains(searchQuery, ignoreCase = true) ||
                model.provider.contains(searchQuery, ignoreCase = true) ||
                model.descriptionFa.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("models_directory_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Directory Header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = ActiveBorder
            ) {
                Text(
                    text = "دایرکتوری کلاستر مدل‌های ORION AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "مشاهده قابلیت‌ها، پنجره زمینه (Context Window)، تاخیر و نرخ مصرف بیش از ۱۵ مدل پیشرو جهان",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی نام مدل، کمپانی سازنده یا قابلیت...", color = TextTertiary) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricIndigo,
                    unfocusedBorderColor = TranslucentBorder,
                    focusedContainerColor = SurfaceElevation1,
                    unfocusedContainerColor = SurfaceElevation1,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ModelCategory.values()) { category ->
                    CyberChip(
                        text = category.labelFa,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }

        // Models List
        items(filteredModels) { model ->
            ModelCard(
                model = model,
                onClick = { selectedModelDialog = model }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Detail Dialog
    if (selectedModelDialog != null) {
        val model = selectedModelDialog!!
        AlertDialog(
            onDismissRequest = { selectedModelDialog = null },
            containerColor = SurfaceElevation2,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = model.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = OutfitFontFamily,
                            color = TextPrimary
                        )
                        Text(
                            text = model.provider,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo
                        )
                    }
                    StatusPill(label = "آنلاین • آماده سرویس‌دهی")
                }
            },
            text = {
                Column {
                    Text(
                        text = model.descriptionFa,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = TranslucentBorder)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("پنجره زمینه:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(model.contextWindow, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("میانگین تاخیر:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${model.latencyMs}ms", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = StatusConnected, fontFamily = OutfitFontFamily)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تعرفه مصرف:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(model.costDesc, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CyberViolet)
                    }
                }
            },
            confirmButton = {
                CyberButton(
                    text = "آزمودن مدل در استودیو",
                    onClick = {
                        selectedModelDialog = null
                        if (model.category == ModelCategory.VIDEO) onNavigateToVideo() else onNavigateToChat()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedModelDialog = null }) {
                    Text("بستن", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ModelCard(
    model: AIModelInfo,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_card_${model.id}"),
        backgroundColor = SurfaceElevation1,
        borderColor = TranslucentBorder,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = OutfitFontFamily,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceElevation3
                    ) {
                        Text(
                            text = model.provider,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = model.descriptionFa,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    maxLines = 2
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusPill(label = "${model.latencyMs}ms")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "★ ${model.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusWarning,
                    fontFamily = OutfitFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                model.tags.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricIndigo.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = model.costDesc,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}
