package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.model.HistoryItem
import com.example.model.HistoryType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HistoryScreen() {
    val historyItems by PlatformRepository.historyItems.collectAsState()
    var selectedType by remember { mutableStateOf(HistoryType.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = historyItems.filter { item ->
        val matchesType = selectedType == HistoryType.ALL || item.type == selectedType
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true) ||
                item.modelName.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = ActiveBorder
            ) {
                Text(
                    text = "تاریخچه فعالیت‌های ORION AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "آرشیو ماندگار کلیه گفتگوهای چت، تصاویر تولیدی، ویدیوها و فایل‌های صوتی سنتز شده",
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
                placeholder = { Text("جستجو در تاریخچه پرامپت‌ها و نام مدل‌ها...", color = TextTertiary) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
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

        // Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryType.values()) { type ->
                    CyberChip(
                        text = type.labelFa,
                        isSelected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "موردی در این دسته‌بندی یافت نشد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredItems) { item ->
                HistoryRowCard(item = item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun HistoryRowCard(item: HistoryItem) {
    val icon = when (item.type) {
        HistoryType.CHAT -> Icons.Default.ChatBubble
        HistoryType.IMAGE -> Icons.Default.AutoFixHigh
        HistoryType.VIDEO -> Icons.Default.Videocam
        HistoryType.AUDIO -> Icons.Default.GraphicEq
        HistoryType.ALL -> Icons.Default.Folder
    }

    val iconColor = when (item.type) {
        HistoryType.CHAT -> ElectricIndigo
        HistoryType.IMAGE -> PhotonBlue
        HistoryType.VIDEO -> CyberViolet
        HistoryType.AUDIO -> StatusConnected
        HistoryType.ALL -> TextPrimary
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${item.id}"),
        backgroundColor = SurfaceElevation1,
        borderColor = TranslucentBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مدل: ${item.modelName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.tokenOrCredit,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                PlatformRepository.dismissAlert()
                                // Download asset action
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "دانلود", tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                // Copy item info
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "کپی", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

