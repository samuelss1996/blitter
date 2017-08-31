package es.soutullo.blitter.model.dao.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import es.soutullo.blitter.model.BlitterSqlDbContract.PersonEntry
import es.soutullo.blitter.model.BlitterSqlDbContract.Table.PERSON
import es.soutullo.blitter.model.dao.PersonDao
import es.soutullo.blitter.model.dao.sql.helper.BlitterSqlDbHelper
import es.soutullo.blitter.model.dao.sql.helper.SqlUtils
import es.soutullo.blitter.model.vo.person.Person

class SqlPersonDao(private val context: Context) : PersonDao {
    private val dbHelper = BlitterSqlDbHelper(this.context)

    override fun queryRecentPersons(limit: Int, exclude: List<Person>): List<Person> {
        val persons = mutableListOf<Person>()
        var query = "SELECT * FROM %s %s ORDER BY %s DESC LIMIT ?"
        val whereClause = when(exclude.isNotEmpty()) {
            true -> String.format("WHERE %s NOT IN (" + exclude.map { "?" }.reduce { acc, s -> acc + "," + s  } + ")", PersonEntry.NAME.colName)
            false -> ""
        }

        query = String.format(query, PERSON.tableName, whereClause, PersonEntry.LAST_DATE.colName)

        this.dbHelper.readableDatabase.rawQuery(query, exclude.map { it.name }.toTypedArray() + arrayOf(limit.toString())).use { cursor ->
            while(cursor.moveToNext()) {
                persons.add(SqlUtils.cursorToPerson(cursor))
            }
        }

        return persons
    }

    override fun insertRecentPerson(person: Person) {
        val values = ContentValues()
        val updateWhere = "${PersonEntry.NAME.colName} LIKE '${person.name}'"
        values.put(PersonEntry.NAME.colName, person.name)
        values.put(PersonEntry.LAST_DATE.colName, person.lastDate.time)

        this.dbHelper.writableDatabase.insertWithOnConflict(PERSON.tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        this.dbHelper.writableDatabase.update(PERSON.tableName, values, updateWhere, null)
    }
}