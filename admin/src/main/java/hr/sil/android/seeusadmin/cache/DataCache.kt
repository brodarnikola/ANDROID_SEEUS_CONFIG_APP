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

package hr.sil.android.seeusadmin.cache

import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RButtonUnit
import hr.sil.android.mplhuber.core.remote.model.RLanguage
import hr.sil.android.mplhuber.core.remote.model.RStationUnit
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.cache.dto.CRegistration
import hr.sil.android.seeusadmin.cache.dto.RLockerUnitDTO
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * @author szuzul
 */
//object DataCache {
//
//    private val buttonUnitsCache by lazy {
//        AutoCache.Builder(
//                TwoLevelCache
//                        .Builder(RLockerUnitDTO::class, RLockerUnitDTO::masterUnitMac)
//                        .memoryLruMaxSize(100)
//                        .build(App.ref))
//                .enableNetworkChecking(App.ref)
//                .setSingleElementSource(CacheSource.ForKey.Suspendable(1, TimeUnit.MINUTES) { masterMacAddress, _ ->
//                    val availableLockerUnits = WSSeeUsAdmin.getButtons(masterMacAddress)
//                            ?: listOf()
//                    RLockerUnitDTO(masterMacAddress, availableLockerUnits)
//                })
//                .build()
//    }
//
//    private val stationUnitCache by lazy {
//        AutoCache.Builder(
//                TwoLevelCache
//                        .Builder(RStationUnit::class, { id })
//                        .memoryLruMaxSize(100)
//                        .build(App.ref))
//                .enableNetworkChecking(App.ref)
//                .setFullSource(CacheSource.ForCache.Suspendable(1, TimeUnit.MINUTES) {
//                    val stationUnits = WSSeeUsAdmin.getStationUnits() ?: listOf()
//                    log.info("Station size: ${stationUnits.size}")
//                    stationUnits
//
//                })
//                .build()
//    }
//
//    private val languagesCache by lazy {
//        AutoCache.Builder(
//                TwoLevelCache
//                        .Builder(RLanguage::class, RLanguage::id)
//                        .memoryLruMaxSize(10)
//                        .build(App.ref))
//                .enableNetworkChecking(App.ref)
//                .setFullSource(CacheSource.ForCache.Suspendable(1, TimeUnit.HOURS) { _ ->
//                    val languages = mutableListOf<RLanguage>()
//                    languages.add(RLanguage())
//                    languages.add(RLanguage(2, "DE", "Germany"))
//                    languages.toList()
//                })
//                .build()
//    }
//
//    suspend fun getButtonUnits(masterMacAddress: String, awaitUpdate: Boolean = false): Collection<RButtonUnit> =
//            buttonUnitsCache.get(masterMacAddress, awaitUpdate)
//                    ?.lockerUnits ?: listOf()
//
//    val log = logger()
//
//    fun clearCaches() {
//        buttonUnitsCache.clear()
//        languagesCache.clear()
//        stationUnitCache.clear()
//    }
//
//    suspend fun preloadCaches() {
//        getLanguages(true)
//        getStationUnits(true)
//
//    }
//
//    suspend fun getStationUnits(awaitUpdate: Boolean = false): Collection<RStationUnit> =
//            stationUnitCache.getAll(awaitUpdate)
//
//    suspend fun getLanguages(awaitUpdate: Boolean = false): Collection<RLanguage> =
//            languagesCache.getAll(awaitUpdate).filter { it.code == "EN" || it.code == "DE" }
//
//    private val registrationStatusDb by lazy {
//        AutoCache.Builder(TwoLevelCache
//                .Builder(CRegistration::class, CRegistration::masterUnitMac)
//                .memoryLruMaxSize(20)
//                .build(App.ref)).setSingleElementSource(CacheSource.ForKey.Suspendable(10, TimeUnit.MINUTES) { mac, it ->
//            null
//        }).build()
//    }
//
//    fun getRegistrationStatusDB(awaitUpdate: Boolean = false): Collection<CRegistration> {
//        return runBlocking {
//            registrationStatusDb.getAll(awaitUpdate)
//        }
//    }
//
//    fun setRegistrationStatus(registrationItem: CRegistration) {
//        registrationStatusDb.put(registrationItem)
//    }
//
//    fun removeRegistrationStatus(masterUnitMac: String) {
//        registrationStatusDb.del(masterUnitMac)
//    }
//
//}

