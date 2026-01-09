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

package hr.sil.android.ble.scanner.scan_multi.util.extensions

import java.util.*


/**
 * @author mfatiga
 */
fun String.toHexCleanUppercase(): String {
    val allowedChars = "0123456789ABCDEF".toCharArray()

    //remove non-hex chars
    val cleanHexChars = this.uppercase(Locale.getDefault()).filter { it in allowedChars }

    //take even number of chars
    return cleanHexChars.take((cleanHexChars.length / 2) * 2)
}

/**
 * @author mfatiga
 */
fun String.hexCleanToBytes(): Array<Byte> {
    //cleanup hex
    val chars = this.toHexCleanUppercase().toCharArray()
    if (chars.isEmpty()) return arrayOf()

    //create result array
    val result = Array(chars.size / 2) { 0x00.toByte() }

    //convert to bytes
    var i = 0
    while (i < chars.size) {
        result[i / 2] = ((Character.digit(chars[i], 16) shl 4) + Character.digit(chars[i + 1], 16)).toByte()
        i += 2
    }
    return result
}