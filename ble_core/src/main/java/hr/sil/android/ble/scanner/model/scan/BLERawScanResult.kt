package hr.sil.android.ble.scanner.model.scan

/**
 * @author mfatiga
 */
data class BLERawScanResult(
        val deviceAddress: String,
        val deviceName: String?,
        val rssi: Int,
        val realTimeMillis: Long,
        val timestampMillis: Long,
        val scanRecord: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        if (other !is BLERawScanResult) return false

        if (deviceAddress != other.deviceAddress
                || deviceName != other.deviceName
                || rssi != other.rssi
                || realTimeMillis != other.realTimeMillis
                || timestampMillis != other.timestampMillis
                || !scanRecord.contentEquals(other.scanRecord)
        ) return false

        return true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + deviceAddress.hashCode()
        hash = 31 * hash + (deviceName?.hashCode() ?: 0)
        hash = 31 * hash + rssi.hashCode()
        hash = 31 * hash + realTimeMillis.hashCode()
        hash = 31 * hash + timestampMillis.hashCode()
        hash = 31 * hash + scanRecord.contentHashCode()
        return hash
    }
}