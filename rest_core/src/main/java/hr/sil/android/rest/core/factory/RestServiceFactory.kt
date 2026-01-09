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

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import rx.schedulers.Schedulers
import java.util.*
import kotlin.reflect.KClass

object RestServiceFactory {
    fun <T : Any> create(serviceClass: KClass<T>, baseUrl: String, getHeaders: () -> List<Pair<String, String>>): T {
        //construct interceptor
        val interceptor = Interceptor { chain ->
            run {
                val requestBuilder = chain.request().newBuilder()
                val headers = getHeaders()
                if (headers.isNotEmpty()) {
                    headers.forEach {
                        requestBuilder.addHeader(it.first, it.second)
                    }
                }
                chain.proceed(requestBuilder.build())
            }
        }

        //construct client builder
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.interceptors().add(interceptor)
        clientBuilder.cache(null)

        //construct converter factories
        val scalarsConverterFactory = ScalarsConverterFactory.create()

        val gsonConverterFactory = GsonConverterFactory.create(GsonBuilder()
                .registerTypeAdapter(Date::class.java, GsonUTCDateAdapter())
                .create())
        //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        //instantiate the service
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(scalarsConverterFactory)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(clientBuilder.build())
                .build()
                .create(serviceClass.java)
    }
}