package com.dharmabit.notes.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dharmabit.notes.data.NoteColor

@Composable
fun ColorPicker(
    selectedColor: NoteColor,
    onColorSelected: (NoteColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        ) {
            items(NoteColor.values()) { color ->
                ColorOption(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: NoteColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val colorValue = getNoteColor(color)

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(colorValue)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun getNoteColor(noteColor: NoteColor): Color {
    return when (noteColor) {
        NoteColor.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
        NoteColor.RED -> Color(0xFFEF5350)
        NoteColor.ORANGE -> Color(0xFFFF9800)
        NoteColor.YELLOW -> Color(0xFFFFEB3B)
        NoteColor.GREEN -> Color(0xFF66BB6A)
        NoteColor.BLUE -> Color(0xFF42A5F5)
        NoteColor.PURPLE -> Color(0xFFAB47BC)
        NoteColor.PINK -> Color(0xFFEC407A)
    }
}

@Composable
fun getNoteColorWithAlpha(noteColor: NoteColor, isDarkTheme: Boolean): Color {
    val baseColor = getNoteColor(noteColor)
    return if (noteColor == NoteColor.DEFAULT) {
        baseColor
    } else {
        baseColor.copy(alpha = if (isDarkTheme) 0.3f else 0.2f)
    }
}