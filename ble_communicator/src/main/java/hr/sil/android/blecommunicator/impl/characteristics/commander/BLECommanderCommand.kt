package hr.sil.android.blecommunicator.impl.characteristics.commander

/**
 * @author mfatiga
 */
data class BLECommanderCommand(private val commandGroup: Int, private val commandAction: Int) {
    fun bytes() = byteArrayOf(commandGroup.toByte(), commandAction.toByte())
}