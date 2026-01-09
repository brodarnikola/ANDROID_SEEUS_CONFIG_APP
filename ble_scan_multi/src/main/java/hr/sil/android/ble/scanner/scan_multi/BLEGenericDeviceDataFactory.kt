/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
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

package hr.sil.android.ble.scanner.scan_multi

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.BLEDeviceDataFactory
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.parser.btcore.BTCoreDataType
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.ble.scanner.rssi.DistanceCalculator
import hr.sil.android.ble.scanner.scan_multi.model.BLEAdvProtocolVersion
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceType
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer
import hr.sil.android.ble.scanner.scan_multi.properties.BLEAdvPropertiesUnknown
import hr.sil.android.ble.scanner.scan_multi.properties.advv3.BLEAdvDynamic
import hr.sil.android.ble.scanner.scan_multi.util.extensions.toInt

/**
 * @author mfatiga
 */
class BLEGenericDeviceDataFactory : BLEDeviceDataFactory<BLEDeviceData> {
    private fun getManufacturer(rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): BLEManufacturer =
            BLEManufacturer.parse(rawScanResult.scanRecord.drop(5).take(2).toByteArray())

    private fun getTxPower(deviceType: BLEDeviceType, advProperties: BLEAdvProperties, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): Int? {
        val txPower = when {
            deviceType.protocolVersion == BLEAdvProtocolVersion.V3 && advProperties is BLEAdvDynamic -> {
                advProperties.extractTxPower(rawScanResult)
            }
            deviceType.protocolVersion in listOf(BLEAdvProtocolVersion.V2, BLEAdvProtocolVersion.V1) -> {
                rawScanResult.scanRecord.drop(30).take(1).toInt()
            }
            deviceType.protocolVersion == BLEAdvProtocolVersion.V0 -> {
                rawScanResult.scanRecord.drop(29).take(1).toInt()
            }
            else -> {
                btCoreAdvertisement.blocks.firstOrNull {
                    it.dataType == BTCoreDataType.TX_POWER_LEVEL
                }?.value?.firstOrNull()?.toInt()
            }
        }

        return if (txPower != null && txPower > 127) txPower - 256 else txPower
    }

    private fun createAdvProperties(manufacturer: BLEManufacturer, deviceType: BLEDeviceType, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): BLEAdvProperties {
        val properties = deviceType.createProperties()
        properties.update(manufacturer, rawScanResult, btCoreAdvertisement)
        return properties
    }

    override fun create(aggregatedRssi: Int, rawScanResults: List<BLERawScanResult>, previousDeviceData: BLEDeviceData?): BLEDeviceData? {
        var isInitial = previousDeviceData == null

        var resultManufacturer = previousDeviceData?.manufacturer ?: BLEManufacturer.UNKNOWN
        var resultDeviceType = previousDeviceData?.deviceType ?: BLEDeviceType.UNKNOWN
        var resultProperties = previousDeviceData?.properties ?: BLEAdvPropertiesUnknown()
        var resultTxPower = previousDeviceData?.txPower
        var resultLastKnownPacketTimestamp = previousDeviceData?.lastKnownPacketTimestamp ?: System.currentTimeMillis()
        var resultLastAdvertisement = previousDeviceData?.lastAdvertisement ?: BTCoreAdvertisement.create(byteArrayOf())

        for (rawScanResult in rawScanResults) {
            val btCoreAdvertisement = BTCoreAdvertisement.create(rawScanResult.scanRecord)
            resultLastAdvertisement = btCoreAdvertisement

            val manufacturer = getManufacturer(rawScanResult, btCoreAdvertisement)
            val deviceType = BLEDeviceType.getDeviceType(manufacturer, rawScanResult, btCoreAdvertisement)

            if (isInitial) {
                //create initial device data from first packet

                isInitial = false
                resultManufacturer = manufacturer
                resultDeviceType = deviceType
                resultProperties = createAdvProperties(resultManufacturer, deviceType, rawScanResult, btCoreAdvertisement)
                resultTxPower = getTxPower(deviceType, resultProperties, rawScanResult, btCoreAdvertisement)
                resultLastKnownPacketTimestamp = System.currentTimeMillis()
            } else {
                if (deviceType == resultDeviceType) {
                    //if device type is not changed -> update

                    resultProperties.update(resultManufacturer, rawScanResult, btCoreAdvertisement)
                    resultTxPower = getTxPower(deviceType, resultProperties, rawScanResult, btCoreAdvertisement)
                    resultLastKnownPacketTimestamp = System.currentTimeMillis()
                } else {
                    if (deviceType.protocolVersion.priority >= resultDeviceType.protocolVersion.priority) {
                        //if protocol is of the same or higher priority than previous -> recreate

                        resultManufacturer = manufacturer
                        resultDeviceType = deviceType
                        resultProperties = createAdvProperties(resultManufacturer, deviceType, rawScanResult, btCoreAdvertisement)
                        resultTxPower = getTxPower(deviceType, resultProperties, rawScanResult, btCoreAdvertisement)
                        resultLastKnownPacketTimestamp = System.currentTimeMillis()
                    } else {
                        val lastKnownPacketAge = System.currentTimeMillis() - resultLastKnownPacketTimestamp
                        if (lastKnownPacketAge > 30_000L) {
                            //if last packet from higher priority was more than 30 seconds ago -> recreate

                            resultManufacturer = manufacturer
                            resultDeviceType = deviceType
                            resultProperties = createAdvProperties(resultManufacturer, deviceType, rawScanResult, btCoreAdvertisement)
                            resultTxPower = getTxPower(deviceType, resultProperties, rawScanResult, btCoreAdvertisement)
                            resultLastKnownPacketTimestamp = System.currentTimeMillis()
                        }
                    }
                }
            }
        }

        if (resultTxPower == null) {
            resultTxPower = previousDeviceData?.txPower
        }

        val distance = resultTxPower?.let {
            DistanceCalculator.calculate(aggregatedRssi, it)
        } ?: 0.0

        return BLEDeviceData(
                manufacturer = resultManufacturer,
                deviceType = resultDeviceType,
                lastKnownPacketTimestamp = resultLastKnownPacketTimestamp,
                txPower = resultTxPower ?: 0,
                distance = distance,
                lastAdvertisement = resultLastAdvertisement,
                properties = resultProperties)
    }
}