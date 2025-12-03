package com.rajamohan.fluxshare.di

import android.content.Context
import com.rajamohan.fluxshare.data.network.TCPTransferHandler
import com.rajamohan.fluxshare.data.network.UDPDiscoveryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Qualifier
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @DeviceId
    fun provideDeviceId(@ApplicationContext context: Context): String {
        val prefs = context.getSharedPreferences("fluxshare_prefs", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }

        return deviceId
    }

    @Provides
    @Singleton
    @DeviceName
    fun provideDeviceName(@ApplicationContext context: Context): String {
        val prefs = context.getSharedPreferences("fluxshare_prefs", Context.MODE_PRIVATE)
        var deviceName = prefs.getString("device_name", null)

        if (deviceName == null) {
            deviceName = android.os.Build.MODEL ?: "Android Device"
            prefs.edit().putString("device_name", deviceName).apply()
        }

        return deviceName
    }

    @Provides
    @Singleton
    @ServerPort
    fun provideServerPort(@ApplicationContext context: Context): Int {
        val prefs = context.getSharedPreferences("fluxshare_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("server_port", 8888)
    }

    @Provides
    @Singleton
    fun provideUDPDiscoveryService(
        @ApplicationContext context: Context,
        @DeviceId deviceId: String,
        @DeviceName deviceName: String,
        @ServerPort port: Int
    ): UDPDiscoveryService {
        return UDPDiscoveryService(context, deviceId, deviceName, port)
    }

    @Provides
    @Singleton
    fun provideTCPTransferHandler(): TCPTransferHandler {
        return TCPTransferHandler()
    }
}