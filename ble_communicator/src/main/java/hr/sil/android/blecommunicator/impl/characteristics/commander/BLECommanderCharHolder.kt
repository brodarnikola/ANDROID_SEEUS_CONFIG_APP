package hr.sil.android.blecommunicator.impl.characteristics.commander

import hr.sil.android.blecommunicator.core.BLECharacteristicsHolder
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.model.BLEReadResult
import hr.sil.android.blecommunicator.core.model.BLEWriteResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take

/**
 * @author mfatiga
 */
class BLECommanderCharHolder(handle: BLECommDeviceHandle) : BLECharacteristicsHolder(handle) {
    //characteristics
    val charCommandWrite by characteristic("1a800001-4448-4fbc-8391-41a8137cfbc3", 244)
    val charCommandRead by characteristic("1a800002-4448-4fbc-8391-41a8137cfbc3", 244)

    //----- READ -----
    suspend fun resetRead(): Boolean =
            charCommandRead.write(byteArrayOf(0x00)).status

    suspend fun beginRead(command: BLECommanderCommand, parameters: ByteArray): Boolean =
            if (resetRead()) charCommandRead.write(command.bytes() + parameters.take(18)).status else false

    suspend fun readFlow(command: BLECommanderCommand, parameters: ByteArray) =
            if (beginRead(command, parameters)) charCommandRead.readFlow() else null

    suspend fun readArray(command: BLECommanderCommand, parameters: ByteArray, maxSize: Int = 0): BLEReadResult {
        val flow = readFlow(command, parameters)
        return if (flow != null) {
            val data = flow.take(maxSize).reduce { accumulator, value -> accumulator + value }
            BLEReadResult(data, true)
        } else {
            BLEReadResult(byteArrayOf(), false)
        }
    }
    //----------------

    //----- WRITE -----
    suspend fun resetWrite(): Boolean =
            charCommandWrite.write(byteArrayOf(0x00)).status

    suspend fun beginWrite(command: BLECommanderCommand, parameters: ByteArray): Boolean =
            if (resetWrite()) charCommandWrite.write(command.bytes() + parameters.take(18)).status else false

    suspend fun writeArray(command: BLECommanderCommand, parameters: ByteArray, data: ByteArray): BLEWriteResult =
            if (beginWrite(command, parameters)) charCommandWrite.write(data) else BLEWriteResult(0L, false)

    suspend fun writeFlow(command: BLECommanderCommand, parameters: ByteArray, flow: Flow<ByteArray>): BLEWriteResult =
            if (beginWrite(command, parameters)) charCommandWrite.writeFlow(flow) else BLEWriteResult(0L, false)

    suspend fun writeEmpty(): BLEWriteResult = charCommandWrite.write(byteArrayOf())

    fun getMaxWriteSize(): Int =
            charCommandWrite.getMaxWriteSize()
    //-----------------
}