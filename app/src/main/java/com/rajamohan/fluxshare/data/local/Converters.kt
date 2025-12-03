package com.rajamohan.fluxshare.data.local

import androidx.room.TypeConverter
import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.domain.model.TransferState

class Converters {
    @TypeConverter
    fun fromTransferState(state: TransferState): String = state.name

    @TypeConverter
    fun toTransferState(value: String): TransferState = TransferState.valueOf(value)

    @TypeConverter
    fun fromTransferDirection(direction: TransferDirection): String = direction.name

    @TypeConverter
    fun toTransferDirection(value: String): TransferDirection = TransferDirection.valueOf(value)
}