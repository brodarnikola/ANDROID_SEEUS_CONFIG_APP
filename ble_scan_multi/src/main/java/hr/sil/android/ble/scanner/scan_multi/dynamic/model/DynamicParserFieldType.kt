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

package hr.sil.android.ble.scanner.scan_multi.dynamic.model

import kotlin.reflect.KClass

/**
 * @author mfatiga
 */
enum class DynamicParserFieldType(val clazz: KClass<*>, val allowMath: Boolean) {
    UINT8_T(Int::class, true),         // 1 byte unsigned number
    UINT16_T(Int::class, true),        // 2 bytes unsigned number
    UINT32_T(Long::class, true),       // 4 bytes unsigned number
    INT8_T(Int::class, true),          // 1 byte signed number
    INT16_T(Int::class, true),         // 2 bytes signed number
    INT32_T(Long::class, true),        // 4 bytes signed number
    FLOAT(Float::class, true),         // 4 bytes decimal number

    EPOCH(Long::class, false),         // 4 bytes unix timestamp in s
    TIMER(Long::class, false),         // n bytes in s

    BOOL(Boolean::class, false),       // 1 byte true or false
    ASCII(String::class, false),       // n bytes ascii string
    HEX(String::class, false),         // n bytes hex string
    FLAGS(BooleanArray::class, false), // 1 byte bits
    FLAG(Boolean::class, false),       // 1 byte with bit mask
}