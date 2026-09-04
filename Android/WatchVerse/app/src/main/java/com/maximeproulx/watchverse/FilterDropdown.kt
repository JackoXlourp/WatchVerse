package com.maximeproulx.watchverse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterDropdown(
    filters: List<String>,
    selectedFilters: Set<String>,
    onApply: (Set<String>) -> Unit
) {
    var pendingSelection by remember(selectedFilters) {
        mutableStateOf(selectedFilters)
    }

    Column(
        modifier = Modifier
            .width(250.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xEE242424))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        (listOf("All") + filters).forEach { filter ->

            val isSelected =
                if (filter == "All") {
                    pendingSelection.isEmpty() ||
                            pendingSelection.size == filters.size
                } else {
                    pendingSelection.isEmpty() ||
                            pendingSelection.contains(filter)
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        pendingSelection =
                            if (filter == "All") {
                                if (pendingSelection.size == filters.size) {
                                    emptySet()
                                } else {
                                    filters.toSet()
                                }
                            } else {
                                if (pendingSelection.contains(filter)) {
                                    pendingSelection - filter
                                } else {
                                    pendingSelection + filter
                                }
                            }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSelected) "●" else "○",
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = filter
                        .replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { word ->
                            word.replaceFirstChar { it.uppercase() }
                        },
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        HorizontalDivider(
            color = Color.White.copy(alpha = 0.18f)
        )

        Text(
            text = "Apply",
            color = Color(
                red = 0.87f,
                green = 0.74f,
                blue = 0.28f
            ),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onApply(pendingSelection)
                }
                .padding(vertical = 4.dp)
        )
    }
}