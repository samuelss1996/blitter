package es.soutullo.blitter.model.dao.sql.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 *
 */
class BlitterSqlDbHelper(context: Context) : SQLiteOpenHelper(context, "Bills.db", null, 1) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

    }
}