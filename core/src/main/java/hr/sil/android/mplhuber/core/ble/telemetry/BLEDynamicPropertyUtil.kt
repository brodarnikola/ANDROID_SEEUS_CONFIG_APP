package hr.sil.android.mplhuber.core.ble.telemetry

import hr.sil.android.ble.scanner.scan_multi.properties.advv3.BLEAdvDynamic
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvPropertyValue
import hr.sil.android.mplhuber.core.util.logger

object BLEDynamicPropertyUtil {
    const val PROP_BATTERY_VOLTAGE = "BATTERY_VOLTAGE"
    const val PROP_MODEM_RSSI = "MODEM_RSSI"
    const val PROP_NUM_OF_BUTTONS = "NUM_OF_BTN_REGISTERED"

    const val PROP_STM_FW_VERSION = "VERSION2"
    const val PROP_MODEM_QUEUE_SIZE = "MODEM_QUEUE_SIZE"

    const val DEVICE_STATUS = "DEVICE_STATUS"
    const val MODEM_STATUS = "MODEM_STATUS"
    const val MODEM_RAT = "MODEM_RAT"

    val log = logger()
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> dynamicProp(props: BLEAdvDynamic, key: String): BLEAdvPropertyValue<T> {
        val prop = props[key]
        val propValue = prop?.getValue()
        val raw = propValue?.raw
        val isUpdated = propValue?.isUpdated ?: false

        return when (T::class) {
            Double::class -> {
                BLEAdvPropertyValue(prop?.asDoubleValue(), raw,  isUpdated) as BLEAdvPropertyValue<T>
            }
            Int::class -> {
                BLEAdvPropertyValue(prop?.asIntValue(), raw,  isUpdated) as BLEAdvPropertyValue<T>
            }
            Long::class -> {
                BLEAdvPropertyValue(prop?.asLongValue(), raw,  isUpdated) as BLEAdvPropertyValue<T>
            }
            Float::class -> {
                BLEAdvPropertyValue(prop?.asFloatValue(), raw,  isUpdated) as BLEAdvPropertyValue<T>
            }

            else -> {
                BLEAdvPropertyValue(null, raw, isUpdated)
            }
        }
    }
}