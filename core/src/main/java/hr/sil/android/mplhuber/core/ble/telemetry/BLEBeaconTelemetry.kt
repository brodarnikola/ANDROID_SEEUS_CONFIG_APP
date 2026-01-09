/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.mplhuber.core.ble.telemetry

import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MyAidLEDStates
import hr.sil.android.ble.scanner.scan_multi.properties.advv3.BLEAdvDynamic
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvPropertyValue
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.DEVICE_STATUS
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.MODEM_RAT
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.MODEM_STATUS
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.PROP_BATTERY_VOLTAGE
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.PROP_MODEM_QUEUE_SIZE
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.PROP_MODEM_RSSI
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.PROP_NUM_OF_BUTTONS
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.PROP_STM_FW_VERSION
import hr.sil.android.mplhuber.core.ble.telemetry.BLEDynamicPropertyUtil.dynamicProp
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.util.general.extensions.format
import hr.sil.android.util.general.extensions.toHexString
import java.util.*

/**
 * @author mfatiga
 */
class BLEBeaconTelemetry private constructor(props: BLEAdvProperties) {
    class Property<T>(
            val label: String,
            private val displayConverter: (T?) -> String? = { it?.toString() }) {

        @Volatile
        private var mValue: T? = null
        val value: T?
            get() = mValue

        @Volatile
        private var mHex: String? = null
        val hex: String?
            get() = mHex

        @Volatile
        private var mDisplay: String? = null
        val display: String?
            get() = mDisplay

        private fun set(value: T?, hex: String?, display: String?) {
            this.mValue = value
            this.mHex = hex
            this.mDisplay = display
        }

        internal fun <Prop> set(prop: BLEAdvPropertyValue<Prop>, transform: (Prop?) -> T?) {
            val value = transform.invoke(prop.value)
            set(value, prop.raw?.toHexString(), displayConverter.invoke(value))
        }

        internal fun set(prop: BLEAdvPropertyValue<T>) {
            set(prop.value, prop.raw?.toHexString(), displayConverter.invoke(prop.value))
        }

        internal fun clear() = set(null, null, null)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Property<*>

            if (label != other.label) return false
            if (value != other.value) return false
            if (hex != other.hex) return false
            if (display != other.display) return false

            return true
        }

        override fun hashCode(): Int {
            var result = label.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
            result = 31 * result + (hex?.hashCode() ?: 0)
            result = 31 * result + (display?.hashCode() ?: 0)
            return result
        }
    }

    companion object {
        //display converters
        private fun getTimestampDisplay(ts: Long?): String? =
                if (ts != null) Date(ts).format("d.MM.yyyy HH:mm:ss") else null

        private fun getVoltageDisplay(value: Double?, unit: String, digits: Int): String? =
                if (value != null) value.format(digits) + unit else "NaN"

        private fun getModemDisplay(value: Int?, unit: String): String =
                if (value != null && value != 0 && value != -1) value.toString() + unit else "NaN"

        private fun getFormatDisplay(value: Int?, unit: String): String =
                if (value != null) value.toString() + unit else "NaN"


        private fun getModemDisplay(value: Long?, unit: String): String? =
                if (value != null) value.toString() + unit else null

        private fun getBooleanDisplay(value: Boolean?): String? =
                if (value != null) if (value) "YES" else "NO" else null

        private fun getStatusDisplay(value: DeviceStatus?): String? = value?.name

        private fun getModemStatusDisplay(value: ModemStatus?): String? = value?.name

        private fun getLedStatesDisplay(value: MyAidLEDStates?): String? = if (value != null) {
            "${value.inProximity}, ${value.boardingAssistance}, ${value.prioritySeating}, ${value.stopTheBus}"
        } else null


        @JvmStatic
        fun create(props: BLEAdvProperties) = BLEBeaconTelemetry(props)
    }


    //battery
    val batteryVoltage = Property<Double>("BAT Voltage") { getVoltageDisplay(it, " V", 2) }
    val modemRssi = Property<Int>("Modem RSSI") { getModemDisplay(it, " db") }
    var modemRat = ""
    var stmVer = ""
    var modemStatus = ""
    var modemQueue = ""
    val numOfButtons = Property<Int>("Number of buttons") { getFormatDisplay(it, "") }
    val deviceStatus = Property<DeviceStatus>("Scu device status") { getStatusDisplay(it) }


    //dynamic
    val dynamic: BLEAdvDynamic?
    val log = logger()
    private fun checkDynamicProperties(props: BLEAdvDynamic): BLEAdvDynamic {

        batteryVoltage.set(dynamicProp(props, PROP_BATTERY_VOLTAGE))
        modemRssi.set(dynamicProp(props, PROP_MODEM_RSSI))
        numOfButtons.set(dynamicProp(props, PROP_NUM_OF_BUTTONS))
        stmVer = props[PROP_STM_FW_VERSION]?.displayValue ?: ""
        modemQueue = props[PROP_MODEM_QUEUE_SIZE]?.displayValue ?: ""
        modemRat = props[MODEM_RAT]?.displayValue ?: ""

        val deviceStatusProp = props[DEVICE_STATUS]
        modemStatus = ModemStatus.parse(props[MODEM_STATUS]?.displayValue).name
        val raw = deviceStatusProp?.getValue()?.raw
        val isUpdated = deviceStatusProp?.getValue()?.isUpdated ?: false
        val deviceStatusValue = BLEAdvPropertyValue(DeviceStatus.parse(deviceStatusProp?.asIntValue()), raw,  isUpdated)
        deviceStatus.set(deviceStatusValue)

        return props
    }

    init {
        //handle dynamic advertisement
        dynamic = if (props is BLEAdvDynamic) checkDynamicProperties(props) else null


    }
}