package com.rajamohan.fluxshare.domain.repository

import com.rajamohan.fluxshare.data.network.UDPDiscoveryService
import com.rajamohan.fluxshare.domain.model.Device
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val discoveryService: UDPDiscoveryService
) {

    @OptIn(InternalSerializationApi::class)
    val discoveredDevices: StateFlow<List<Device>> = discoveryService.discoveredDevices

    fun startDiscovery(scope: kotlinx.coroutines.CoroutineScope) {
        discoveryService.startDiscovery(scope)
    }

    fun stopDiscovery() {
        discoveryService.stopDiscovery()
    }
}