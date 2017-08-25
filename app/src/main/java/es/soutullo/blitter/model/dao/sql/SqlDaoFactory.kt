package es.soutullo.blitter.model.dao.sql

import android.content.Context
import es.soutullo.blitter.model.dao.BillDao
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.dao.PersonDao

class SqlDaoFactory(context: Context) : DaoFactory(context) {
    override fun getBillDao(): BillDao = SqlBillDao(this.context)
    override fun getPersonDao(): PersonDao = SqlPersonDao(this.context)
}