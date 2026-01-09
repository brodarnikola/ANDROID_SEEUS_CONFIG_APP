package hr.sil.android.ble.scanner.scan_multi.properties.advv2

import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.NinaResetReason
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvMplSlaveP16 : BLEAdvV2Base() {
    val batteryValueAdc by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(14, 15)),
            BLEAdvPropertyRaw { it })

    val doorStatus by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(16, 17)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { bitmask ->
                if (bitmask != null) Array(16) { ((bitmask shr (15 - it)) and 0x01) > 0 } else null
            })

    val uptime by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(18, 21)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.LITTLE_ENDIAN, false) { it })

    val resetReason by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(22)),
            BLEAdvPropertyRaw { NinaResetReason.parse(it.firstOrNull()) })
}