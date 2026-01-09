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

package hr.sil.android.rest.core.configuration.parameters

import hr.sil.android.rest.core.configuration.ServiceConfig
import hr.sil.android.rest.core.configuration.base.AbstractConfigContainer
import hr.sil.android.rest.core.configuration.parameters.model.Authorization
import hr.sil.android.rest.core.configuration.parameters.model.RestHeader
import java.io.File

open class ServiceParameters(serviceClassName: String) : AbstractConfigContainer(File(ServiceConfig.configBaseDirectory, "service_$serviceClassName.json")) {
    private var baseUrl by delegate("http://127.0.0.1/") {
        if (it.endsWith("/")) it else it + "/"
    }

    private var baseUrlContext by delegate("") {
        if (it.startsWith("/")) {
            if (it.length > 1) it.substring(1)
            else ""
        } else it
    }

    fun setBaseURL(url: String, serviceContext: String = ""): ServiceParameters {
        this.baseUrl = url
        this.baseUrlContext = serviceContext
        return this
    }

    fun getBaseURL(withContext: Boolean = true): String {
        return if (withContext) baseUrl + baseUrlContext else baseUrl
    }

    private val headerKeyPrefix = "_header_"
    private val headerNameWrapLeft = "_<"
    private val headerNameWrapRight = ">_"
    fun getHeaders(): List<Pair<String, String>> {
        return getAll(headerKeyPrefix).toList().map { (mapKey, headerValue) ->
            val headerKey = mapKey.substring(mapKey.indexOf(headerNameWrapRight) + headerNameWrapRight.length)
            headerKey to headerValue
        }
    }

    fun <T : RestHeader> setHeader(name: String, header: T): ServiceParameters {
        val headerKey = "$headerKeyPrefix$headerNameWrapLeft$name$headerNameWrapRight${header.key}"
        val headerValue = header.value
        set(headerKey, headerValue)
        return this
    }

    inline fun <reified T : Authorization> setAuthorization(auth: T): ServiceParameters {
        setHeader(T::class.java.canonicalName, auth)
        return this
    }
}