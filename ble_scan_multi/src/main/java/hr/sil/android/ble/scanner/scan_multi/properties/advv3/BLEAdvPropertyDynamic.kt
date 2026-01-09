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

package hr.sil.android.ble.scanner.scan_multi.properties.advv3

import hr.sil.android.ble.scanner.scan_multi.dynamic.BLEDynamicParser
import hr.sil.android.ble.scanner.scan_multi.dynamic.model.DynamicParserField
import hr.sil.android.ble.scanner.scan_multi.dynamic.model.DynamicParserFieldType
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperty
import hr.sil.android.ble.scanner.scan_multi.util.extensions.*
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

/**
 * @author mfatiga
 */
class BLEAdvPropertyDynamic(fieldDefinition: DynamicParserField) : BLEAdvProperty<ByteArray>() {
    companion object {
        private const val MATH_VARIABLE = "x"

        //util
        private fun Date.format(format: String) = SimpleDateFormat(format).format(this)
        private fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
    }

    //copied field definition props
    private val fieldDefinitionType = fieldDefinition.type!!
    private val fieldDefinitionEnumerate = fieldDefinition.enumerate
    private val fieldDefinitionPrefix = fieldDefinition.prefix ?: ""
    private val fieldDefinitionSuffix = fieldDefinition.suffix ?: ""
    private val fieldDefinitionMeasUnit = fieldDefinition.measUnit ?: ""
    private val fieldDefinitionArraySeparator = fieldDefinition.arraySeparator ?: ""
    private val fieldDefinitionMask = fieldDefinition.maskHex?.hexCleanToBytes()?.firstOrNull()

    //static
    val fieldKey: String = fieldDefinition.key
    val fieldSortPosition: Int? = fieldDefinition.sortPosition
    val displayLabel: String = fieldDefinition.label
    val showInCompact: Boolean = fieldDefinition.showInCompact ?: false

    //dynamic
    /**
     * The constructed display value according to the field definition.
     * Constructed as:
     *   - convert all parsed values to string
     *   - for all values:
     *     - check if the hex value of the specified bytes is found in the enums:
     *       - if it is, use the defined display value
     *       - otherwise use the string value
     *   - join the resulting string values with the defined arraySeparator
     *   - if prefix and/or suffix are defined, prepend and/or append to the joined value
     */
    var displayValue: String = ""
        get() = synchronized(this) { field }
        private set(value) = synchronized(this) { field = value }


    /**
     * The resulting parsed value array. Mostly, this array will contain only one value, unless
     * the definition contains more byte indices than is required for one value to be constructed.
     * For example, if the defined value is of type UINT8_T and 4 byte indices are defined:
     *  - the resulting value array will contain 4 values of type "Int" if "Int" or no math is
     *    preformed, or 4 values of type "Double" if "Double" math is performed
     */
    var values: Array<Any> = arrayOf()
        get() = synchronized(this) { field }
        private set(value) = synchronized(this) { field = value }

    /**
     * The first value of the parsed values array.
     */
    val firstValue: Any?
        get() = values.firstOrNull()

    /**
     * The resulting class of the values in the value array.
     * Can be one of:
     *   - when defined as a signed or unsigned numeric value:
     *     - "Int" or "Long" when defined as "Int" or "Long" and "Int" or no math is performed
     *     - "Float" when defined as "Float" and no math is performed
     *     - "Double" when defined as a numeric value and "Double" math is performed
     *   - when defined as EPOCH or TIMER:
     *     - always "Long"
     *   - when defined as BOOL or FLAG:
     *     - always "Boolean"
     *   - when defined as FLAGS:
     *     - always "BooleanArray"
     *   - when defined as ASCII or HEX:
     *     - always "String"
     */
    val valueClass: KClass<*>

    /**
     * Tries to cast the value to the reified T type if the valueClass is of that type.
     *
     * Returns null if:
     *  - value is null
     *  - valueClass is not equal to class of T
     *  - class of value is not equal to class of T
     */
    inline fun <reified T : Any> castOrNull(value: Any?): T? {
        return if (value != null && T::class == valueClass) {
            value as? T?
        } else null
    }

    /**
     * Tries to cast the first value in the value array to the reified T type if the valueClass is
     * of that type.
     *
     * Returns null if:
     *  - value is null
     *  - valueClass is not equal to class of T
     *  - class of value is not equal to class of T
     */
    inline fun <reified T : Any> castValueOrNull(): T? = castOrNull(firstValue)

    /**
     * Returns the first value in array converted to Float if it is of a numeric type.
     */
    fun asFloatValue(): Float? {
        val value = firstValue
        return if (value != null) {
            when (value) {
                is Int -> value.toFloat()
                is Long -> value.toFloat()
                is Float -> value
                is Double -> value.toFloat()
                else -> null
            }
        } else null
    }

    /**
     * Returns the first value in array converted to Double if it is of a numeric type.
     */
    fun asDoubleValue(): Double? {
        val value = firstValue
        return if (value != null) {
            when (value) {
                is Int -> value.toDouble()
                is Long -> value.toDouble()
                is Float -> value.toDouble()
                is Double -> value
                else -> null
            }
        } else null
    }

    /**
     * Returns the first value in array converted to Int if it is of a numeric type.
     */
    fun asIntValue(): Int? {
        val value = firstValue
        return if (value != null) {
            when (value) {
                is Int -> value
                is Long -> value.toInt()
                is Float -> value.toInt()
                is Double -> value.toInt()
                else -> null
            }
        } else null
    }

    /**
     * Returns the first value in array converted to Long if it is of a numeric type.
     */
    fun asLongValue(): Long? {
        val value = firstValue
        return if (value != null) {
            when (value) {
                is Int -> value.toLong()
                is Long -> value
                is Float -> value.toLong()
                is Double -> value.toLong()
                else -> null
            }
        } else null
    }

    /**
     * Returns the first value in array converted to BigDecimal from Double if it is of a numeric type.
     */
    fun asBigDecimal(): BigDecimal? {
        val value = asDoubleValue()
        return if (value != null) BigDecimal.valueOf(value) else null
    }

    /**
     * Returns the first value in array converted to BigInteger from Long if it is of a numeric type.
     */
    fun asBigInteger(): BigInteger? {
        val value = asLongValue()
        return if (value != null) BigInteger.valueOf(value) else null
    }

    //math and class
    private enum class MathValuesType {
        UNDEFINED,
        INTEGER,
        DOUBLE
    }

    private fun getMathValueType(strValue: String?): MathValuesType {
        return when {
            strValue?.toIntOrNull() != null -> MathValuesType.INTEGER
            strValue?.toDoubleOrNull() != null -> MathValuesType.DOUBLE
            else -> MathValuesType.UNDEFINED
        }
    }

    private var mathExpression: Expression? = null
    private val mathAddFirst = fieldDefinition.addFirst ?: false
    private val mathPrecision = fieldDefinition.precision
    private val mathMultiplierInt: Int?
    private val mathMultiplierDouble: Double?
    private val mathAddendInt: Int?
    private val mathAddendDouble: Double?
    private val mathType: MathValuesType

    private fun calculateExpression(arg: Double): Double? {
        val mathExpr = mathExpression
        return if (mathExpr != null) {
            mathExpr.setArgumentValue(MATH_VARIABLE, arg)
            try {
                mathExpr.calculate()
            } catch (ex: Exception) {
                null
            }
        } else null
    }

    //display wrappers
    private fun wrapEnumValue(stringValue: String, hexValue: String): String {
        val hexUppercase = hexValue.toHexCleanUppercase()
        val enumValue = fieldDefinitionEnumerate?.firstOrNull { it.hex.toHexCleanUppercase() == hexUppercase }
        return enumValue?.display ?: "$stringValue$fieldDefinitionMeasUnit"
    }

    private fun updateDisplayValue(stringValues: List<String>, hexValues: List<String>) {
        //merge values by first converting enums and then joining to string with configured
        // array separator
        val merged = stringValues
                .mapIndexed { idx, str -> wrapEnumValue(str, hexValues[idx]) }
                .joinToString(fieldDefinitionArraySeparator) { it }

        //update string result value with added prefix and suffix
        displayValue = "$fieldDefinitionPrefix$merged$fieldDefinitionSuffix"
    }

    //update and parse
    private fun updateValues(res: List<Any>, str: List<String>, hex: List<String>) {
        values = res.toTypedArray()
        updateDisplayValue(str, hex)
    }

    private fun byteToHex(byte: Byte): String = String.format("%02X", byte)

    private data class Parsed(val res: Any, val str: String, val hex: String)

    private fun parseByChunk(bytes: ByteArray, chunkSize: Int, parse: ((ByteArray) -> Parsed)) {
        val res = mutableListOf<Any>()
        val str = mutableListOf<String>()
        val hex = mutableListOf<String>()
        for (chunk in bytes.toList().chunked(chunkSize.coerceAtLeast(1)).map { it.toByteArray() }) {
            val (r, s, h) = parse(chunk)
            res.add(r)
            str.add(s)
            hex.add(h)
        }
        updateValues(res, str, hex)
    }

    private fun parseByByte(bytes: ByteArray, parse: ((Byte) -> Parsed)) {
        val res = mutableListOf<Any>()
        val str = mutableListOf<String>()
        val hex = mutableListOf<String>()
        for (byte in bytes) {
            val (r, s, h) = parse(byte)
            res.add(r)
            str.add(s)
            hex.add(h)
        }
        updateValues(res, str, hex)
    }

    private fun parseFloats(bytes: ByteArray, byteOrder: ByteOrder) {
        parseByChunk(bytes, 4) { chunk ->
            val hex = chunk.toHexString()
            val inValue = ByteBuffer
                    .wrap(chunk.pad(4, 0x00.toByte(), byteOrder))
                    .order(byteOrder)
                    .getFloat(0)

            val mathExpr = mathExpression
            if (mathExpr != null) {
                val inValueDouble = inValue.toDouble()
                val res = calculateExpression(inValueDouble) ?: inValueDouble
                val str = if (mathPrecision != null) res.format(mathPrecision) else res.toString()
                Parsed(res, str, hex)
            } else {
                //try parse as either int or double
                val multiplier = mathMultiplierInt?.toDouble() ?: mathMultiplierDouble
                val addend = mathAddendInt?.toDouble() ?: mathAddendDouble
                if (multiplier != null || addend != null) {
                    var value = inValue.toDouble()
                    if (mathAddFirst) {
                        if (addend != null) value += addend
                        if (multiplier != null) value *= multiplier
                    } else {
                        if (multiplier != null) value *= multiplier
                        if (addend != null) value += addend
                    }
                    val str = if (mathPrecision != null) value.format(mathPrecision) else value.toString()
                    Parsed(
                            res = value,
                            str = str,
                            hex = hex)
                } else {
                    val str = if (mathPrecision != null) inValue.toDouble().format(mathPrecision) else inValue.toString()
                    Parsed(
                            res = inValue,
                            str = str,
                            hex = hex)
                }
            }
        }
    }

    private fun parseInteger(bytes: ByteArray, size: Int, isSigned: Boolean, byteOrder: ByteOrder) {
        var valueSize = size.coerceIn(1, 4)
        if (valueSize == 3) valueSize = 4

        parseByChunk(bytes, valueSize) { chunk ->
            val hex = chunk.toHexString()

            //wrap to int or long
            val parseSize = if (valueSize < 4) 4 else 8

            //fill byte buffer
            val byteBuffer = ByteBuffer
                    .wrap(chunk.pad(parseSize, 0x00.toByte(), byteOrder))
                    .order(byteOrder)

            //calculate max signed and unsigned value for the requested value size
            val maxValueShiftBits = 8 * (valueSize - 1)

            //extract math values
            val mulInt = mathMultiplierInt ?: 1
            val mulDbl = mathMultiplierDouble ?: 1.0
            val addInt = mathAddendInt ?: 0
            val addDbl = mathAddendDouble ?: 0.0

            if (parseSize == 8) { //Long
                val inValue = byteBuffer.getLong(0)
                val parsedValue = if (!isSigned) {
                    inValue
                } else {
                    val signedMax = 0x7FL shlOnes maxValueShiftBits
                    val unsignedMax = 0xFFL shlOnes maxValueShiftBits
                    if (inValue <= signedMax) inValue else (inValue - unsignedMax - 1)
                }

                val mathExpr = mathExpression
                if (mathExpr != null) {
                    val parsedValueDouble = parsedValue.toDouble()
                    val res = calculateExpression(parsedValueDouble) ?: parsedValueDouble
                    val str = if (mathPrecision != null) res.format(mathPrecision) else res.toString()
                    Parsed(res, str, hex)
                } else when (mathType) {
                    MathValuesType.INTEGER -> {
                        val res = if (mathAddFirst) {
                            (parsedValue + addInt.toLong()) * mulInt.toLong()
                        } else {
                            (parsedValue * mulInt.toLong()) + addInt.toLong()
                        }
                        Parsed(res, res.toString(), hex)
                    }
                    MathValuesType.DOUBLE -> {
                        val res = if (mathAddFirst) {
                            (parsedValue.toDouble() + addDbl) * mulDbl
                        } else {
                            (parsedValue.toDouble() * mulDbl) + addDbl
                        }
                        val str = if (mathPrecision != null) res.format(mathPrecision) else res.toString()
                        Parsed(res, str, hex)
                    }
                    else -> {
                        Parsed(parsedValue, parsedValue.toString(), hex)
                    }
                }
            } else { //Int
                val inValue = byteBuffer.getInt(0)
                val parsedValue = if (!isSigned) {
                    inValue
                } else {
                    val signedMax = 0x7F shlOnes maxValueShiftBits
                    val unsignedMax = 0xFF shlOnes maxValueShiftBits
                    if (inValue <= signedMax) inValue else (inValue - unsignedMax - 1)
                }

                val mathExpr = mathExpression
                if (mathExpr != null) {
                    val parsedValueDouble = parsedValue.toDouble()
                    val res = calculateExpression(parsedValueDouble) ?: parsedValueDouble
                    val str = if (mathPrecision != null) res.format(mathPrecision) else res.toString()
                    Parsed(res, str, hex)
                } else when (mathType) {
                    MathValuesType.INTEGER -> {
                        val res = if (mathAddFirst) {
                            (parsedValue + addInt) * mulInt
                        } else {
                            (parsedValue * mulInt) + addInt
                        }
                        Parsed(res, res.toString(), hex)
                    }
                    MathValuesType.DOUBLE -> {
                        val res = if (mathAddFirst) {
                            (parsedValue.toDouble() + addDbl) * mulDbl
                        } else {
                            (parsedValue.toDouble() * mulDbl) + addDbl
                        }
                        val str = if (mathPrecision != null) res.format(mathPrecision) else res.toString()
                        Parsed(res, str, hex)
                    }
                    else -> {
                        Parsed(parsedValue, parsedValue.toString(), hex)
                    }
                }
            }
        }
    }

    override fun parse(bytes: ByteArray): ByteArray {
        val byteOrder = BLEDynamicParser.byteOrder
        val dateFormat = BLEDynamicParser.dateFormat
        val boolToString = BLEDynamicParser.booleanToString
        when (fieldDefinitionType) {
            DynamicParserFieldType.UINT8_T -> {
                parseInteger(bytes, 1, false, byteOrder)
            }
            DynamicParserFieldType.UINT16_T -> {
                parseInteger(bytes, 2, false, byteOrder)
            }
            DynamicParserFieldType.UINT32_T -> {
                parseInteger(bytes, 4, false, byteOrder)
            }
            DynamicParserFieldType.INT8_T -> {
                parseInteger(bytes, 1, true, byteOrder)
            }
            DynamicParserFieldType.INT16_T -> {
                parseInteger(bytes, 2, true, byteOrder)
            }
            DynamicParserFieldType.INT32_T -> {
                parseInteger(bytes, 4, true, byteOrder)
            }
            DynamicParserFieldType.FLOAT -> {
                parseFloats(bytes, byteOrder)
            }
            DynamicParserFieldType.EPOCH -> {
                parseByChunk(bytes, 4) { chunk ->
                    val inValue = ByteBuffer
                            .wrap(chunk.pad(8, 0x00.toByte(), byteOrder))
                            .order(byteOrder)
                            .getLong(0)

                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.timeInMillis = inValue * 1000L
                    calendar.timeZone = TimeZone.getDefault()
                    val value = calendar.timeInMillis

                    Parsed(
                            res = value,
                            str = Date(value).format(dateFormat),
                            hex = chunk.toHexString())
                }
            }
            DynamicParserFieldType.TIMER -> {
                parseByChunk(bytes, 4) { chunk ->
                    val value = ByteBuffer
                            .wrap(chunk.pad(8, 0x00.toByte(), byteOrder))
                            .order(byteOrder)
                            .getLong(0)

                    var remain = value
                    val hours = remain / 3600
                    remain -= hours * 3600
                    val minutes = remain / 60
                    remain -= minutes * 60
                    val seconds = remain

                    val hoursStr = hours.toString().padStart(2, '0')
                    val minutesStr = minutes.toString().padStart(2, '0')
                    val secondsStr = seconds.toString().padStart(2, '0')

                    Parsed(
                            res = value,
                            str = "$hoursStr:$minutesStr:$secondsStr",
                            hex = chunk.toHexString())
                }
            }
            DynamicParserFieldType.BOOL -> {
                parseByByte(bytes) { byte ->
                    val value = byte > 0
                    Parsed(
                            res = value,
                            str = boolToString(value),
                            hex = byteToHex(byte))
                }
            }
            DynamicParserFieldType.ASCII -> {
                parseByByte(bytes) { byte ->
                    val value = byte.toInt().toChar()
                    Parsed(
                            res = value,
                            str = value.toString(),
                            hex = byteToHex(byte)
                    )
                }
            }
            DynamicParserFieldType.HEX -> {
                parseByByte(bytes) { byte ->
                    val value = byteToHex(byte)
                    Parsed(
                            res = value,
                            str = value,
                            hex = value)
                }
            }
            DynamicParserFieldType.FLAGS -> {
                parseByByte(bytes) { byte ->
                    val value = Array(8) { (byte.toInt() and ( 1 shl (7 - it))) > 0 }.toBooleanArray()
                    Parsed(
                            res = value,
                            str = value.joinToString("") { if (it) "1" else "0" },
                            hex = byteToHex(byte))
                }

            }
            DynamicParserFieldType.FLAG -> {
                val mask = fieldDefinitionMask ?: 0xFF.toByte()
                parseByByte(bytes) { byte ->
                    val masked = byte.toInt() and mask.toInt()
                    val value = masked > 0
                    Parsed(
                            res = value,
                            str = boolToString(value),
                            hex = byteToHex(masked.toByte()))
                }
            }
        }

        //return raw default result
        return bytes
    }

    init {
        if (fieldDefinitionType.allowMath) {
            try {
                val mathExpr = (fieldDefinition.math ?: "").trim()
                if (mathExpr.isNotEmpty()) {
                    val argument = Argument("$MATH_VARIABLE = 1")
                    val expression = Expression(mathExpr, argument)
                    if (expression.checkSyntax()) {
                        mathExpression = expression
                    }
                }
            } catch (exc: Exception) {
                //ignore
            }

            //legacy only if expression is undefined
            if (mathExpression != null) {
                mathMultiplierDouble = null
                mathAddendDouble = null
                mathMultiplierInt = null
                mathAddendInt = null
                mathType = MathValuesType.DOUBLE
                valueClass = Double::class
            } else {
                val strMultiplier = fieldDefinition.multiplier
                val multiplierType = getMathValueType(strMultiplier)

                val strAddend = fieldDefinition.addend
                val addendType = getMathValueType(strAddend)

                when {
                    multiplierType == MathValuesType.INTEGER && addendType == MathValuesType.INTEGER -> {
                        mathMultiplierInt = strMultiplier?.toIntOrNull()
                        mathAddendInt = strAddend?.toIntOrNull()
                        mathType = MathValuesType.INTEGER
                        valueClass = fieldDefinitionType.clazz

                        mathMultiplierDouble = null
                        mathAddendDouble = null
                    }
                    multiplierType == MathValuesType.INTEGER && addendType == MathValuesType.DOUBLE -> {
                        mathMultiplierDouble = strMultiplier?.toIntOrNull()?.toDouble()
                        mathAddendDouble = strAddend?.toDoubleOrNull()
                        mathType = MathValuesType.DOUBLE
                        valueClass = Double::class

                        mathMultiplierInt = null
                        mathAddendInt = null
                    }
                    multiplierType == MathValuesType.DOUBLE && addendType == MathValuesType.INTEGER -> {
                        mathMultiplierDouble = strMultiplier?.toDoubleOrNull()
                        mathAddendDouble = strAddend?.toIntOrNull()?.toDouble()
                        mathType = MathValuesType.DOUBLE
                        valueClass = Double::class

                        mathMultiplierInt = null
                        mathAddendInt = null
                    }
                    multiplierType == MathValuesType.DOUBLE && addendType == MathValuesType.DOUBLE -> {
                        mathMultiplierDouble = strMultiplier?.toDoubleOrNull()
                        mathAddendDouble = strAddend?.toDoubleOrNull()
                        mathType = MathValuesType.DOUBLE
                        valueClass = Double::class

                        mathMultiplierInt = null
                        mathAddendInt = null
                    }
                    else -> {
                        mathMultiplierDouble = null
                        mathAddendDouble = null
                        mathMultiplierInt = null
                        mathAddendInt = null
                        mathType = MathValuesType.UNDEFINED
                        valueClass = fieldDefinitionType.clazz
                    }
                }
            }
        } else {
            mathMultiplierInt = null
            mathMultiplierDouble = null
            mathAddendInt = null
            mathAddendDouble = null
            mathType = MathValuesType.UNDEFINED
            valueClass = fieldDefinitionType.clazz
        }
    }
}