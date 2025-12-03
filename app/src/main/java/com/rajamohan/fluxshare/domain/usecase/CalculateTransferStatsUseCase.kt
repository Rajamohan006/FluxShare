package com.rajamohan.fluxshare.domain.usecase

import com.rajamohan.fluxshare.domain.model.TransferStats

class CalculateTransferStatsUseCase {

    private val speedSamples = mutableListOf<Pair<Long, Long>>() // timestamp to bytes
    private val maxSamples = 10

    fun calculateStats(
        completedChunks: Int,
        totalChunks: Int,
        transferredBytes: Long,
        fileSize: Long,
        startTime: Long
    ): TransferStats {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime

        // Calculate progress
        val progress = if (totalChunks > 0) completedChunks.toFloat() / totalChunks else 0f

        // Calculate current speed
        val currentSpeed = if (elapsedTime > 0) {
            (transferredBytes * 1000) / elapsedTime
        } else 0L

        // Calculate average speed with moving window
        speedSamples.add(currentTime to transferredBytes)
        if (speedSamples.size > maxSamples) {
            speedSamples.removeAt(0)
        }

        val averageSpeed = if (speedSamples.size >= 2) {
            val first = speedSamples.first()
            val last = speedSamples.last()
            val timeDiff = last.first - first.first
            val bytesDiff = last.second - first.second
            if (timeDiff > 0) (bytesDiff * 1000) / timeDiff else 0L
        } else currentSpeed

        // Calculate ETA
        val remainingBytes = fileSize - transferredBytes
        val eta = if (averageSpeed > 0) {
            (remainingBytes * 1000) / averageSpeed
        } else 0L

        return TransferStats(
            currentSpeed = currentSpeed,
            averageSpeed = averageSpeed,
            estimatedTimeRemaining = eta,
            progressPercentage = progress
        )
    }

    fun reset() {
        speedSamples.clear()
    }
}