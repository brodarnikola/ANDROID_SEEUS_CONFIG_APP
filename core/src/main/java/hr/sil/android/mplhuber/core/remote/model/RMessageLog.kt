/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author szuzul
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

package hr.sil.android.mplhuber.core.remote.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * @author szuzul
 */
class RMessageLog {
    var id: Int = 0

    @SerializedName("account___id")
    var accountId: Int = 0
    var isAvailable: Boolean = false

    var subject: String = ""
    var body: String = ""

    var dateCreated: Date = Date()


    @SerializedName("master___name")
    var master___name: String = ""

}