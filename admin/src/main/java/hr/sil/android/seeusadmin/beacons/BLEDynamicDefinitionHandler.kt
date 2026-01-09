package hr.sil.android.seeusadmin.beacons

import android.content.Context
import hr.sil.android.ble.scanner.scan_multi.dynamic.BLEDynamicParser
import hr.sil.android.ble.scanner.scan_multi.dynamic.model.*
//import hr.sil.android.datacache.AutoCache
//import hr.sil.android.datacache.TwoLevelCache
//import hr.sil.android.datacache.updatable.CacheSource
//import hr.sil.android.datacache.util.PersistenceClassTracker
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import ru.gildor.coroutines.retrofit.await
import java.util.concurrent.TimeUnit

/**
 * @author mfatiga
 */
object BLEDynamicDefinitionHandler  {

    val log = logger()
    /*
    val defSeeUs = DynamicParserDefinition(
        key = "SEE_US",
        offset = 7,
        deviceTypeIndices = arrayOf(22),
        deviceTypeHex = "4B",
        packetCodeIndex = 0,

        latitude = "SEE-US",
        color = "green",
        txPower = arrayOf(DynamicParserTxPower(
                packetCodesHex = "73,74",
                index = 23
        )),
        fields = arrayOf(
                DynamicParserField(
                        //define
                        key = "LIGHT_SENSOR",
                        packetCodesHex = "73",

                        //index
                        type = DynamicParserFieldType.FLOAT,
                        index = arrayOf(9, 10, 11, 12),

                        //parse
                        multiplier = "1",
                        addend = "0",
                        addFirst = false,
                        maskHex = null, //hex string - 1 byte, used for "FLAG" type

                        label = "Light sensor",
                        prefix = "",
                        suffix = "",
                        measUnit = " lux",
                        arraySeparator = ", ",
                        precision = 2,
                        enumerate = arrayOf(
                                DynamicParserFieldEnum(
                                        hex = "FFFFFFFF",
                                        display = ""
                                )
                        )
                )
        )
    )
    val definitions = listOf(defSeeUs)
    BLEDynamicParser.updateDefinitions(definitions, System.currentTimeMillis())
    */

    private const val UPDATE_TIME_PERIOD = 10L
    private val UPDATE_TIME_UNIT = TimeUnit.MINUTES

    fun checkClasses(context: Context) {
//        PersistenceClassTracker.checkClass(context, DynamicParserDefinition::class)
//        PersistenceClassTracker.checkClass(context, DynamicParserTxPower::class)
//        PersistenceClassTracker.checkClass(context, DynamicParserField::class)
//        PersistenceClassTracker.checkClass(context, DynamicParserFieldType::class)
//        PersistenceClassTracker.checkClass(context, DynamicParserFieldEnum::class)
    }

    private const val URL_ROOT = "https://dynamicparser.swissinnolab.com/"

    interface DefinitionsService {
            @GET("device_list.json")
        fun getDefinitionNamesList(): Call<List<String>>

        @GET("{definition_file_name}")
        fun getDefinition(@Path("definition_file_name") definitionFileName: String): Call<DynamicParserDefinition>
    }

    private val definitionsService by lazy {
        Retrofit.Builder()
                .baseUrl(URL_ROOT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DefinitionsService::class.java)
    }

    @Volatile
    private var definitionsUpdateTimestamp: Long = 0L
    private val dynamicParserDefinitionCache by lazy {
//        AutoCache.Builder(
//                TwoLevelCache
//                        .Builder(DynamicParserDefinition::class, DynamicParserDefinition::key)
//                        .memoryLruMaxSize(100)
//                        .build(App.ref))
//                .enableNetworkChecking(App.ref)
//                .setFullSource(CacheSource.ForCache.Suspendable(UPDATE_TIME_PERIOD, UPDATE_TIME_UNIT) { _ ->
//                    log.info("Running cache fetch...")
//                    val definitionNames = try {
//                        definitionsService.getDefinitionNamesList().await()
//                    } catch (exc: Exception) {
//                        log.error("Failed to fetch definition file names!", exc)
//                        null
//                    }
//                    log.info("Got definition file names: ${definitionNames?.joinToString(", ") { it }
//                            ?: "NULL"}")
//
//                    val result = mutableListOf<DynamicParserDefinition>()
//                    if (definitionNames != null) {
//                        for (definitionName in definitionNames) {
//                            val definition = try {
//                                definitionsService.getDefinition(definitionName).await()
//                            } catch (exc: Exception) {
//                                log.error("Failed to fetch definition \"$definitionName\"!", exc)
//                                null
//                            }
//                            if (definition != null) {
//                                result.add(definition)
//                            }
//                        }
//                    }
//                    log.info("Fetched: ${result.size} definitions...")
//
//                    val now = System.currentTimeMillis()
//                    definitionsUpdateTimestamp = now
//                    val updated = BLEDynamicParser.updateDefinitions(result, now)
//                    log.info("Accepted: ${updated.size} definitions...")
//                    updated
//                })
//                .build()
    }

    private suspend fun tryGetDefinitions(awaitUpdate: Boolean): List<DynamicParserDefinition> {
        return try {
            //dynamicParserDefinitionCache.getAll(awaitUpdate).toList()
            listOf()
        } catch (exc: Exception) {
            //dynamicParserDefinitionCache.clear()
            listOf()
        }
    }

    private suspend fun checkUpdate(awaitUpdate: Boolean = false) {
        //force set first time
        if (definitionsUpdateTimestamp == 0L) {
            log.info("Forcing update first time...")
            val now = System.currentTimeMillis()
            definitionsUpdateTimestamp = now

            val definitions = tryGetDefinitions(awaitUpdate)
            val updated = BLEDynamicParser.updateDefinitions(definitions, now)
            log.info("Accepted: ${updated.size} definitions...")
        } else {
            log.info("Running regular update...")
            tryGetDefinitions(awaitUpdate)
        }
    }

    private var lastCheck = 0L
    private const val RUN_CHECK_PERIOD = 60_000L
    fun checkUpdateAsync(forceUpdate: Boolean = false) {
        val now = System.currentTimeMillis()
        if (forceUpdate || now - lastCheck > RUN_CHECK_PERIOD) {
            lastCheck = now
            GlobalScope.launch(Dispatchers.IO) {
                checkUpdate(forceUpdate)
            }
        }
    }
}