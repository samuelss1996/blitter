package es.soutullo.blitter.model.dao.sql.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import es.soutullo.blitter.model.BlitterSqlDbContract

class BlitterSqlDbHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        val DB_VERSION = 2
        val DB_NAME = "Bills.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        BlitterSqlDbContract.Table.values().forEach({table ->  db.execSQL(this.createTableQuery(table))})
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        BlitterSqlDbContract.Table.values().reversed().forEach { db.execSQL("DROP TABLE IF EXISTS ${it.tableName};") }
        this.onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        db?.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Generates the creation sql query for a specific table
     * @param table The table to create the query for
     * @return The query as String
     */
    private fun createTableQuery(table: BlitterSqlDbContract.Table): String {
        val queryBuilder = StringBuilder()

        queryBuilder.append(String.format("CREATE TABLE %s ( ", table.tableName))

        for (column in table.columns) {
            val extraAttributes = column.extraAttributes ?: "NOT NULL"
            queryBuilder.append(String.format("%s %s %s,", column.colName, column.type, extraAttributes))
        }

        for ((currentCol, refTable, refCol) in table.foreignKeys) {
            queryBuilder.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE,", currentCol, refTable, refCol))
        }

        return queryBuilder.replace(queryBuilder.lastIndexOf(","), queryBuilder.lastIndexOf(",") + 1, ");").toString()
    }
}