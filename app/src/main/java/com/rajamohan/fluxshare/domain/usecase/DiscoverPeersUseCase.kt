package com.rajamohan.fluxshare.domain.usecase

import com.rajamohan.fluxshare.domain.model.Device
import com.rajamohan.fluxshare.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

class DiscoverPeersUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(scope: kotlinx.coroutines.CoroutineScope) {
        deviceRepository.startDiscovery(scope)
    }

    fun stopDiscovery() {
        deviceRepository.stopDiscovery()
    }

    @OptIn(InternalSerializationApi::class)
    fun getDiscoveredDevices(): StateFlow<List<Device>> {
        return deviceRepository.discoveredDevices
    }
}