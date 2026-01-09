package hr.sil.android.rest.core

//import android.content.Context
//import com.snappydb.DB
//import hr.sil.android.datacache.db.DBMultiFactory
//
///**
// * @author mfatiga
// */
//object CacheDatabase {
//    private var cacheDatabase: DB? = null
//
//    /**
//     * Returns the internal cache database.
//     */
//    internal fun getCacheDatabase(context: Context): DB {
//        synchronized(this) {
//            if (cacheDatabase == null) {
//                cacheDatabase = DBMultiFactory.open(context.applicationContext)
//            }
//            return cacheDatabase!!
//        }
//    }
//
//    /**
//     * Clears the internal cache database.
//     */
//    fun clearCacheDatabase(context: Context) = clearDatabase(getCacheDatabase(context))
//
//
//    private var userDatabase: DB? = null
//
//    /**
//     * Returns/Creates an instance of the user database.
//     */
//    fun getDatabase(context: Context): DB {
//        synchronized(this) {
//            if (userDatabase == null) {
//                userDatabase = DBMultiFactory.open(context.applicationContext, "_usr_")
//            }
//            return userDatabase!!
//        }
//    }
//
//    /**
//     * Clears the user database
//     */
//    fun clearUserDatabase(context: Context) = clearDatabase(getDatabase(context))
//
//    private fun clearDatabase(db: DB) {
//        synchronized(this) {
//            val iterator = db.allKeysIterator()
//            while (iterator.hasNext()) {
//                db.del(iterator.next(1).first())
//            }
//        }
//    }
//}