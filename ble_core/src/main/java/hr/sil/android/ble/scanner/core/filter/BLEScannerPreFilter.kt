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

package hr.sil.android.ble.scanner.core.filter

import android.util.Log
import hr.sil.android.ble.scanner.BuildConfig
import hr.sil.android.ble.scanner.util.toHexString
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author mfatiga
 */
internal object BLEScannerPreFilter {
    private const val TAG = "BLEScannerPreFilter"

    //Lazy initialize build config scan filter
    //filter format: startIndex:compareString|startIndex:compareString
    private val buildConfigFilterMap: Map<Int, String> by lazy {
        val baseScanFilterString = BuildConfig.BASE_SCAN_FILTER   //BuildConfig.BASE_SCAN_FILTER
        Log.i(TAG, "BuildConfigFilter=$baseScanFilterString")
        if (baseScanFilterString.isBlank()) {
            mapOf()
        } else {
            val filterFieldStrings =
                    if (baseScanFilterString.contains("|")) baseScanFilterString.split("|")
                    else listOf(baseScanFilterString)
            filterFieldStrings.mapNotNull {
                val filterFieldStringSplit = if (it.contains(":")) it.split(":") else listOf(it)
                if (filterFieldStringSplit.size == 2) {
                    val byteIndex = filterFieldStringSplit[0].toIntOrNull()
                    val byteValue = filterFieldStringSplit[1]
                    if (byteIndex != null && byteValue.length >= 2 && byteValue.length % 2 == 0) {
                        Pair(byteIndex, byteValue)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.toMap()
        }
    }
    private val checkBuildConfigFilters: Boolean by lazy { buildConfigFilterMap.isNotEmpty() }

    private fun matchesBuildConfigFilter(scanRecord: ByteArray): Boolean {
        return if (checkBuildConfigFilters) {
            val hexRecord = scanRecord.toHexString()
            buildConfigFilterMap.all { (byteIndex, value) ->
                val stringIndex = byteIndex * 2
                if (stringIndex < hexRecord.length) {
                    value == hexRecord.substring(stringIndex, (stringIndex + value.length))
                } else {
                    false
                }
            }
        } else true
    }

    private data class FilterByteIndices(val indices: IntArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FilterByteIndices

            return indices.contentEquals(other.indices)
        }

        override fun hashCode(): Int = Arrays.hashCode(indices)
    }

    private val perByteFilters = ConcurrentHashMap<FilterByteIndices, List<ByteArray>>()
    private fun matchesPerByteFilters(scanRecord: ByteArray): Boolean {
        return if (perByteFilters.isNotEmpty()) {
            var anyMatch = false
            for ((scanRecordByteIndices, filterByteArrays) in perByteFilters) {
                val scanRecordBytes = try {
                    scanRecordByteIndices.indices.map { scanRecord[it] }.toByteArray()
                } catch (exc: Exception) {
                    null
                }
                if (scanRecordBytes != null && filterByteArrays.any { Arrays.equals(it, scanRecordBytes) }) {
                    anyMatch = true
                    break
                }
            }
            anyMatch
        } else true
    }

    fun setCustomFilters(filters: List<Pair<IntArray, ByteArray>>) {
        perByteFilters.clear()
        perByteFilters.putAll(filters.groupBy({ FilterByteIndices(it.first) }, { it.second }))
    }

    fun isValid(scanRecord: ByteArray): Boolean =
            matchesBuildConfigFilter(scanRecord) && matchesPerByteFilters(scanRecord)
}