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

package hr.sil.android.rest.core.factory

import hr.sil.android.rest.core.configuration.ServiceConfig
import hr.sil.android.rest.core.configuration.parameters.ServiceParameters
import kotlin.reflect.KClass

open class RestServiceAccessor<out T : Any>(private val serviceClass: KClass<T>) {
    private var baseUrl: String = ""
    private var _service: T? = null

    /**
     * Service will be recreated if baseUrl changes after initial creation.
     * App key is fetched from ServiceConfig and sent with each request.
     */
    val service: T
        get() {
            if (_service == null || baseUrl != config.getBaseURL()) {
                baseUrl = config.getBaseURL()
                _service = RestServiceFactory.create(
                        serviceClass,
                        baseUrl,
                        { listOf(Pair("AppKey", ServiceConfig.cfg.appKey)) + config.getHeaders() }
                )
            }
            return _service!!
        }

    val config by lazy { ServiceParameters(serviceClass.java.simpleName) }
}