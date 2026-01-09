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

package hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.notifiable

import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharNotifiable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.base.BaseBLECharBehavior
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce

/**
 * @author mfatiga
 */
internal class BLECharNotNotifiable : BaseBLECharBehavior(), BLECharNotifiable {
    override fun isNotifiable() = false

    override fun notifyOnChannel() = notifyOnChannel(Dispatchers.Default)

    override fun notifyOnChannel(dispatcher: CoroutineDispatcher) = GlobalScope.produce<ByteArray>(dispatcher) { close() }
}