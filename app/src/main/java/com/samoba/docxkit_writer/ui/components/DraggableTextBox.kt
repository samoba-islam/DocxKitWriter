package com.samoba.docxkit_writer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samoba.docxkit_writer.model.TextBoxElement

@Composable
fun DraggableTextBox(
    textBox: TextBoxElement,
    isSelected: Boolean,
    pageWidthDp: Float,
    pageHeightDp: Float,
    onSelect: () -> Unit,
    onPositionChange: (Float, Float) -> Unit,
    onSizeChange: (Float, Float) -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // All values in dp for consistent handling
    var offsetXDp by remember { mutableFloatStateOf(textBox.x) }
    var offsetYDp by remember { mutableFloatStateOf(textBox.y) }
    var boxWidthDp by remember { mutableFloatStateOf(textBox.width) }
    var boxHeightDp by remember { mutableFloatStateOf(textBox.height) }
    
    // Sync state when textBox properties change (e.g., after page size change)
    LaunchedEffect(textBox.x, textBox.y, textBox.width, textBox.height) {
        offsetXDp = textBox.x
        offsetYDp = textBox.y
        boxWidthDp = textBox.width
        boxHeightDp = textBox.height
    }
    
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    
    Box(
        modifier = modifier
            .offset(x = offsetXDp.dp, y = offsetYDp.dp)
            .size(width = boxWidthDp.dp, height = boxHeightDp.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .pointerInput(textBox.id) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = { onDoubleClick() }
                )
            }
            .pointerInput(textBox.id, pageWidthDp, pageHeightDp) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Convert pixel drag to dp
                        val dragDpX = with(density) { dragAmount.x.toDp().value }
                        val dragDpY = with(density) { dragAmount.y.toDp().value }
                        
                        // Calculate bounds in dp
                        val minX = 0f
                        val minY = 0f
                        val maxX = (pageWidthDp - boxWidthDp).coerceAtLeast(0f)
                        val maxY = (pageHeightDp - boxHeightDp).coerceAtLeast(0f)
                        
                        offsetXDp = (offsetXDp + dragDpX).coerceIn(minX, maxX)
                        offsetYDp = (offsetYDp + dragDpY).coerceIn(minY, maxY)
                    },
                    onDragEnd = {
                        onPositionChange(offsetXDp, offsetYDp)
                    }
                )
            }
    ) {
        // Text content
        Text(
            text = textBox.text,
            fontSize = textBox.fontSize.sp,
            fontWeight = if (textBox.isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (textBox.isItalic) FontStyle.Italic else FontStyle.Normal,
            color = Color.Black,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
        )
        
        // Resize handle (bottom-right corner)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(textBox.id, pageWidthDp, pageHeightDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Convert pixel drag to dp
                                val dragDpX = with(density) { dragAmount.x.toDp().value }
                                val dragDpY = with(density) { dragAmount.y.toDp().value }
                                
                                // Allow resize within page bounds (in dp)
                                val maxWidth = (pageWidthDp - offsetXDp).coerceAtLeast(50f)
                                val maxHeight = (pageHeightDp - offsetYDp).coerceAtLeast(30f)
                                boxWidthDp = (boxWidthDp + dragDpX).coerceIn(50f, maxWidth)
                                boxHeightDp = (boxHeightDp + dragDpY).coerceIn(30f, maxHeight)
                            },
                            onDragEnd = {
                                onSizeChange(boxWidthDp, boxHeightDp)
                            }
                        )
                    }
            )
        }
    }
}

