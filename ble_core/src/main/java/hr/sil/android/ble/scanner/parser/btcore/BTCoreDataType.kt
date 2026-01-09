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

package hr.sil.android.ble.scanner.parser.btcore

/**
 * @author mfatiga
 */
enum class BTCoreDataType(val code: Byte?) {
    UNKNOWN(null),
    FLAGS(0x01.toByte()),
    INCOMPLETE_LIST_OF_16_BIT_SERVICE_CLASS_UUIDS(0x02.toByte()),
    COMPLETE_LIST_OF_16_BIT_SERVICE_CLASS_UUIDS(0x03.toByte()),
    INCOMPLETE_LIST_OF_32_BIT_SERVICE_CLASS_UUIDS(0x04.toByte()),
    COMPLETE_LIST_OF_32_BIT_SERVICE_CLASS_UUIDS(0x05.toByte()),
    INCOMPLETE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS(0x06.toByte()),
    COMPLETE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS(0x07.toByte()),
    SHORTENED_LOCAL_NAME(0x08.toByte()),
    COMPLETE_LOCAL_NAME(0x09.toByte()),
    TX_POWER_LEVEL(0x0A.toByte()),
    CLASS_OF_DEVICE(0x0D.toByte()),
    SIMPLE_PAIRING_HASH_C(0x0E.toByte()),
    SIMPLE_PAIRING_HASH_C_192(0x0E.toByte()),
    SIMPLE_PAIRING_RANDOMIZER_R(0x0F.toByte()),
    SIMPLE_PAIRING_RANDOMIZER_R_192(0x0F.toByte()),
    DEVICE_ID(0x10.toByte()),
    SECURITY_MANAGER_TK_VALUE(0x10.toByte()),
    SECURITY_MANAGER_OUT_OF_BAND_FLAGS(0x11.toByte()),
    SLAVE_CONNECTION_INTERVAL_RANGE(0x12.toByte()),
    LIST_OF_16_BIT_SERVICE_SOLICITATION_UUIDS(0x14.toByte()),
    LIST_OF_128_BIT_SERVICE_SOLICITATION_UUIDS(0x15.toByte()),
    SERVICE_DATA(0x16.toByte()),
    SERVICE_DATA_16_BIT_UUID(0x16.toByte()),
    PUBLIC_TARGET_ADDRESS(0x17.toByte()),
    RANDOM_TARGET_ADDRESS(0x18.toByte()),
    APPEARANCE(0x19.toByte()),
    ADVERTISING_INTERVAL(0x1A.toByte()),
    LE_BLUETOOTH_DEVICE_ADDRESS(0x1B.toByte()),
    LE_ROLE(0x1C.toByte()),
    SIMPLE_PAIRING_HASH_C_256(0x1D.toByte()),
    SIMPLE_PAIRING_RANDOMIZER_R_256(0x1E.toByte()),
    LIST_OF_32_BIT_SERVICE_SOLICITATION_UUIDS(0x1F.toByte()),
    SERVICE_DATA_32_BIT_UUID(0x20.toByte()),
    SERVICE_DATA_128_BIT_UUID(0x21.toByte()),
    LE_SECURE_CONNECTIONS_CONFIRMATION_VALUE(0x22.toByte()),
    LE_SECURE_CONNECTIONS_RANDOM_VALUE(0x23.toByte()),
    URI(0x24.toByte()),
    INDOOR_POSITIONING(0x25.toByte()),
    TRANSPORT_DISCOVERY_DATA(0x26.toByte()),
    LE_SUPPORTED_FEATURES(0x27.toByte()),
    CHANNEL_MAP_UPDATE_INDICATION(0x28.toByte()),
    PB_ADV(0x29.toByte()),
    MESH_MESSAGE(0x2A.toByte()),
    MESH_BEACON(0x2B.toByte()),
    INFORMATION_DATA_3D(0x3D.toByte()),
    MANUFACTURER_SPECIFIC_DATA(0xFF.toByte());

    companion object {
        fun parse(code: Byte?): BTCoreDataType = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}