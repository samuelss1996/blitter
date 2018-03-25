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

class SqlPersonDao(private val context: Context, private val dbHelper: BlitterSqlDbHelper = BlitterSqlDbHelper(context)) : PersonDao {

    override fun queryRecentPersons(limit: Int, exclude: List<Person>): List<Person> {
        val persons = mutableListOf<Person>()
        var query = "SELECT * FROM %s %s ORDER BY %s DESC LIMIT ?"
        var whereClause = when(exclude.isNotEmpty()) {
            true -> String.format("WHERE %s NOT IN (" + exclude.map { "?" }.reduce { acc, s -> acc + "," + s  } + ") AND ", PersonEntry.NAME.colName)
            false -> "WHERE "
        }

        whereClause += "${PersonEntry.VISIBLE.colName} = 1"
        query = String.format(query, PERSON.tableName, whereClause, PersonEntry.LAST_DATE.colName)

        this.dbHelper.readableDatabase.rawQuery(query, exclude.map { it.name }.toTypedArray() + arrayOf(limit.toString())).use { cursor ->
            while(cursor.moveToNext()) {
                persons.add(SqlUtils.cursorToPerson(cursor))
            }
        }

        return persons
    }

    override fun queryPersonByExactName(name: String): Person? {
        val query = "SELECT * FROM %s WHERE %s LIKE ?".format(PERSON.tableName, PersonEntry.NAME)

        this.dbHelper.readableDatabase.rawQuery(query, arrayOf(name)).use { cursor ->
            if(cursor.moveToNext()) {
                return SqlUtils.cursorToPerson(cursor)
            }
        }

        return null
    }

    override fun insertRecentPerson(person: Person) {
        this.alterRecentPersonVisibility(person.name, person.lastDate.time, true)
    }

    override fun deleteRecentPerson(personName: String) {
        this.alterRecentPersonVisibility(personName, null, false)
    }

    override fun deleteAllPersons() {
        this.dbHelper.writableDatabase.delete(PERSON.tableName, "1=1", null)
    }

    private fun alterRecentPersonVisibility(personName: String, personTime: Long?, visible: Boolean) {
        val values = ContentValues()
        val updateWhere = "${PersonEntry.NAME.colName} LIKE ?"

        values.put(PersonEntry.NAME.colName, personName)
        values.put(PersonEntry.VISIBLE.colName, if(visible) 1 else 0)

        if(personTime != null) {
            values.put(PersonEntry.LAST_DATE.colName, personTime)
        }

        this.dbHelper.writableDatabase.insertWithOnConflict(PERSON.tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        this.dbHelper.writableDatabase.update(PERSON.tableName, values, updateWhere, arrayOf(personName))
    }
}