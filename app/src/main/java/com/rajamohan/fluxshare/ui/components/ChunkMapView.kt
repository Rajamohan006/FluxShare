package com.rajamohan.fluxshare.ui.components

import android.graphics.Canvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.rajamohan.fluxshare.domain.model.ChunkEntity
import com.rajamohan.fluxshare.ui.theme.success
import java.nio.file.Files.size

@Composable
fun ChunkMapView(
    chunks: List<ChunkEntity>,
    totalChunks: Int,
    modifier: Modifier = Modifier
) {
    val completedColor = MaterialTheme.colorScheme.success
    val incompleteColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier.height(40.dp)) {
        val chunkWidth = size.width / totalChunks
        val chunkHeight = size.height

        chunks.forEachIndexed { index, chunk ->
            drawRect(
                color = if (chunk.isCompleted) completedColor else incompleteColor,
                topLeft = Offset(index * chunkWidth, 0f),
                size = Size(chunkWidth - 1.dp.toPx(), chunkHeight)
            )
        }
    }
}