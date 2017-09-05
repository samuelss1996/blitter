package es.soutullo.blitter.model.dao

import android.content.Context
import es.soutullo.blitter.model.dao.sql.SqlDaoFactory

/**
 *
 */
abstract class DaoFactory(protected val context: Context) {
    companion object {

        /**
         * Returns the default DAO factory
         * @param context The Android context
         * @return The DAO factory
         */
        fun getFactory(context: Context): DaoFactory = SqlDaoFactory(context)
    }

    /** @return The bills DAO */
    abstract fun getBillDao(): BillDao

    /** @return The persons DAO */
    abstract fun getPersonDao(): PersonDao
}