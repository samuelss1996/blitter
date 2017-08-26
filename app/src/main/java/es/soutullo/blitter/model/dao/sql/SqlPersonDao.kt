package es.soutullo.blitter.model.dao.sql

import android.content.Context
import es.soutullo.blitter.model.BlitterSqlDbContract.PersonEntry
import es.soutullo.blitter.model.BlitterSqlDbContract.Table.PERSON
import es.soutullo.blitter.model.dao.PersonDao
import es.soutullo.blitter.model.dao.sql.helper.BlitterSqlDbHelper
import es.soutullo.blitter.model.dao.sql.helper.SqlUtils
import es.soutullo.blitter.model.vo.person.Person

class SqlPersonDao(private val context: Context) : PersonDao {
    private val dbHelper = BlitterSqlDbHelper(this.context)

    override fun queryRecentPersons(limit: Int): List<Person> {
        val persons = ArrayList<Person>()
        var query = "SELECT * FROM %s ORDER BY %s LIMIT ?"
        query = String.format(query, PERSON.tableName, PersonEntry.LAST_DATE.colName)

        this.dbHelper.readableDatabase.rawQuery(query, arrayOf(limit.toString())).use { cursor ->
            while(cursor.moveToNext()) {
                persons.add(SqlUtils.cursorToPerson(cursor))
            }
        }

        return persons
    }
}