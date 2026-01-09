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

package hr.sil.android.seeusadmin.util

import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.text.InputFilter
import android.view.View
import android.view.ViewManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

/**
 * @author mfatiga
 */
object DialogUtil {
//    private fun ViewManager.createUI(message: String, innerView: (LinearLayout.() -> View)? = null): View {
//        return verticalLayout {
//            horizontalPadding = resources.getDimensionPixelSize(dip(16))
//            verticalPadding = resources.getDimensionPixelSize(dip(16))
//
//            textView {
//                text = message
//
//                textSize = 16.0F
//                setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
//                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
//            }.lparams {
//                width = matchParent
//                height = matchParent
//            }
//
//            if (innerView != null) {
//                innerView().lparams {
//                    topMargin = dip(5)
//                    width = matchParent
//                }
//            }
//        }
//    }

//    fun messageDialogBuilder(ctx: Context, message: String, cb: (() -> Unit)? = null): AlertBuilder<DialogInterface> {
//        return ctx.alert {
//            customView {
//                createUI(message)
//            }
//            if (cb != null) {
//                positiveButton(ctx.resources.getString(android.R.string.ok)) { cb() }
//                onCancelled { cb() }
//            }
//        }
//    }

//    fun inputDialogBuilder(ctx: Context, message: String, editTextInputType: Int, inputFilters: Array<InputFilter>? = null, cb: (String?) -> Unit): AlertBuilder<DialogInterface> {
//        return ctx.alert {
//            var editTextInput: EditText? = null
//            customView {
//                createUI(message) {
//                    verticalLayout {
//                        editTextInput = editText {
//                            setPadding(dip(20), dip(10), dip(20), dip(10))
//                            textSize = 18.0F
//
//                            singleLine = true
//                            maxLines = 1
//                            inputType = editTextInputType
//                            if (inputFilters != null) {
//                                filters = inputFilters
//                            }
//                        }
//                    }
//                }
//            }
//
//            positiveButton(ctx.resources.getString(android.R.string.ok)) {
//                val inputText = editTextInput?.text?.toString()
//                cb(inputText)
//            }
//            negativeButton(ctx.resources.getString(android.R.string.cancel)) { cb(null) }
//            onCancelled { cb(null) }
//        }
//    }

    data class MultiInputItem(val hintText: String?, val editTextInputType: Int?, val inputFilters: Array<InputFilter>? = null) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MultiInputItem

            if (hintText != other.hintText) return false
            if (editTextInputType != other.editTextInputType) return false
            if (!Arrays.equals(inputFilters, other.inputFilters)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = hintText?.hashCode() ?: 0
            result = 31 * result + (editTextInputType ?: 0)
            result = 31 * result + (inputFilters?.let { Arrays.hashCode(it) } ?: 0)
            return result
        }
    }



//    fun <T> listDialogBuilder(ctx: Context, listItems: List<T>, itemToString: (T) -> String, onItemSelected: (T?) -> Unit): AlertBuilder<DialogInterface> {
//        return ctx.alert {
//            items(listItems.map { itemToString(it) }.toList()) { dialogInterface, _, index ->
//                val item = listItems[index]
//                onItemSelected(item)
//                dialogInterface.dismiss()
//            }
//            onCancelled { onItemSelected(null) }
//        }
//    }
}