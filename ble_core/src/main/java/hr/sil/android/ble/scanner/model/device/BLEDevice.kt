package hr.sil.android.ble.scanner.model.device

import java.util.*

/**
 * @author mfatiga
 */
data class BLEDevice<out D>(
        val deviceAddress: String,
        val deviceName: String?,
        val rssi: Int,
        val packetsPerSecond: Double,
        val lastSeen: Long,
        val lastPacketRealTimeMillis: Long,
        val lastPacketTimestampMillis: Long,
        val lastPacketBytes: ByteArray,
        val data: D) {

    constructor(otherBLEDevice: BLEDevice<*>, otherDeviceData: D) : this(
            otherBLEDevice.deviceAddress,
            otherBLEDevice.deviceName,
            otherBLEDevice.rssi,
            otherBLEDevice.packetsPerSecond,
            otherBLEDevice.lastSeen,
            otherBLEDevice.lastPacketRealTimeMillis,
            otherBLEDevice.lastPacketTimestampMillis,
            otherBLEDevice.lastPacketBytes,
            otherDeviceData
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BLEDevice<*>

        if (deviceAddress != other.deviceAddress) return false
        if (deviceName != other.deviceName) return false
        if (rssi != other.rssi) return false
        if (packetsPerSecond != other.packetsPerSecond) return false
        if (lastSeen != other.lastSeen) return false
        if (lastPacketRealTimeMillis != other.lastPacketRealTimeMillis) return false
        if (lastPacketTimestampMillis != other.lastPacketTimestampMillis) return false
        if (!lastPacketBytes.contentEquals(other.lastPacketBytes)) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceAddress.hashCode()
        result = 31 * result + (deviceName?.hashCode() ?: 0)
        result = 31 * result + rssi
        result = 31 * result + packetsPerSecond.hashCode()
        result = 31 * result + lastSeen.hashCode()
        result = 31 * result + lastPacketRealTimeMillis.hashCode()
        result = 31 * result + lastPacketTimestampMillis.hashCode()
        result = 31 * result + lastPacketBytes.contentHashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }
}