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

package hr.sil.android.rest.core.configuration

import android.content.Context
import hr.sil.android.rest.core.configuration.base.AbstractConfigContainer
import java.io.File
import java.util.*

object ServiceConfig {
    class ConfigContainer(configFile: File) : AbstractConfigContainer(configFile) {
        private var _appKey by delegate("")
        val appKey: String
            get() {
                if (_appKey.isBlank()) {
                    _appKey = UUID.randomUUID().toString()
                }
                return _appKey
            }

        fun resetAppKey() {
            _appKey = ""
        }
    }

    lateinit var configBaseDirectory: File
        private set

    lateinit var cfg: ConfigContainer
        private set

    fun initialize(context: Context): ServiceConfig {
        if (!::configBaseDirectory.isInitialized) {
            configBaseDirectory = File(context.filesDir.absolutePath + "/rest_service_config/")
            configBaseDirectory.mkdirs()
        }
        if (!::cfg.isInitialized) {
            cfg = ConfigContainer(File(configBaseDirectory, "config.json"))
        }
        return this
    }
}
