package hr.sil.android.ble.scanner.scan_multi.dynamic

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.scan_multi.dynamic.model.DynamicParserDefinition
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
object BLEDynamicParser {
    //parsing params
    var booleanToString: ((Boolean) -> String) = { it.toString() }
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    var byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    var dateFormat: String = "d.MM.yyyy HH:mm:ss"
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    private fun isDefinitionValid(definition: DynamicParserDefinition): Boolean {
        if (definition.key.isBlank()) return false
        if (definition.offset < 0) return false
        if (definition.deviceTypeIndices.size != definition.deviceTypeCodes.size) return false
        if (definition.deviceTypeIndices.isEmpty()) return false
        if (definition.deviceTypeCodes.isEmpty()) return false

        return true
    }

    fun filterDefinitions(definitions: List<DynamicParserDefinition>): List<DynamicParserDefinition> {
        //remove invalid and remove duplicates
        return definitions
                .filter { isDefinitionValid(it) }
                .groupBy { definition -> definition.key }
                .mapNotNull { group -> group.value.firstOrNull() }
    }

    fun updateDefinitions(definitions: List<DynamicParserDefinition>, updateTimestamp: Long): List<DynamicParserDefinition> {
        synchronized(this) {
            val filtered = filterDefinitions(definitions)

            this.definitions = filtered
            this.definitionsUpdateTimestamp = updateTimestamp

            return filtered
        }
    }

    //parser definition list
    private var definitions: List<DynamicParserDefinition> = listOf()
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    //last update timestamp - used to check if packets need to be invalidated
    internal var definitionsUpdateTimestamp: Long = 0L
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    //internal utility function to find target definition from the received scan result
    internal fun findDefinition(rawScanResult: BLERawScanResult): DynamicParserDefinition? =
            definitions.firstOrNull { it.matchesDefinition(rawScanResult) }
}