package es.soutullo.blitter.model.dao

/**
 *
 */
abstract class DaoFactory {
    companion object {

        /**
         * @return
         */
        fun getFactory(): DaoFactory {
           TODO("not implemented")
        }
    }

    abstract fun getBillDao(): BillDao
    abstract fun getPersonDao(): PersonDao
}