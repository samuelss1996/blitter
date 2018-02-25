package es.soutullo.blitter.model.dao.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import es.soutullo.blitter.R
import es.soutullo.blitter.model.BlitterSqlDbContract.BillEntry
import es.soutullo.blitter.model.BlitterSqlDbContract.BillLineEntry
import es.soutullo.blitter.model.BlitterSqlDbContract.BillLinePersonEntry
import es.soutullo.blitter.model.BlitterSqlDbContract.PersonEntry
import es.soutullo.blitter.model.BlitterSqlDbContract.Table.*
import es.soutullo.blitter.model.dao.BillDao
import es.soutullo.blitter.model.dao.sql.helper.BlitterSqlDbHelper
import es.soutullo.blitter.model.dao.sql.helper.SqlUtils
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.person.Person
import java.util.*

class SqlBillDao(private val context: Context, private val dbHelper: BlitterSqlDbHelper = BlitterSqlDbHelper(context)) : BillDao {

    override fun queryBills(begin: Int, limit: Int): List<Bill> {
        var queryBills = "SELECT * FROM %s ORDER BY %s ASC, %s DESC LIMIT ?, ?"
        queryBills = String.format(queryBills, BILL.tableName, BillEntry.STATUS.colName, BillEntry.DATE.colName)

        return this.retrieveBillsByQuery(queryBills, kotlin.arrayOf(begin.toString(), limit.toString()))
    }

    override fun searchBills(partialName: String, limit: Int): List<Bill> {
        var queryBills = "SELECT * FROM %s WHERE %s LIKE ? ORDER BY %s DESC LIMIT ?"
        queryBills = String.format(queryBills, BILL.tableName, BillEntry.NAME.colName, BillEntry.DATE.colName)

        return this.retrieveBillsByQuery(queryBills, kotlin.arrayOf("%$partialName%", limit.toString()))
    }

    override fun insertBill(bill: Bill): Long {
        with(this.dbHelper.writableDatabase) {
            this.beginTransaction()
            val billId = this.insert(BILL.tableName, null, generateBillValues(bill))

            for (line in bill.lines) {
                val billLineId = this.insert(BILL_LINE.tableName, null, generateBillLineValues(billId, line))

                for (person in line.persons) {
                    var personId = this.insertWithOnConflict(PERSON.tableName, null, generatePersonValues(person), SQLiteDatabase.CONFLICT_IGNORE)
                    personId = if(personId > -1) personId else SqlPersonDao(context, dbHelper).queryPersonByExactName(person.name)?.id ?: -1

                    this.insert(BILL_LINE_PERSON.tableName, null, generateBillLinePersonValues(billLineId, personId))
                }
            }

            this.setTransactionSuccessful()
            this.endTransaction()

            bill.id = billId
            return billId
        }
    }

    override fun updateBill(billId: Long?, bill: Bill) {
        with(this.dbHelper.writableDatabase) {
            this.beginTransaction()

            deleteBills(if(billId != null) listOf(billId) else listOf())
            insertBill(bill)

            this.setTransactionSuccessful()
            this.endTransaction()
        }
    }

    override fun deleteBills(billsIds: List<Long>) {
        with(this.dbHelper.writableDatabase) {
            val statement = this.compileStatement(String.format("DELETE FROM %s WHERE %s = ?", BILL.tableName, BillEntry._ID.colName))
            this.beginTransaction()

            for (id in billsIds) {
                statement.bindLong(1, id)
                statement.executeUpdateDelete()
            }

            this.setTransactionSuccessful()
            this.endTransaction()
        }
    }

    override fun deleteAllBills() {
        this.dbHelper.writableDatabase.delete(BILL.tableName, "1=1", null)
    }

    override fun cloneBill(billToCloneId: Long) {
        val bill = this.generateClonedBill(billToCloneId)
        val newNamePrefix = this.context.getString(R.string.cloned_bill_prefix)

        bill.name = "$newNamePrefix ${bill.name}"

        this.insertBill(bill)
    }

    /**
     * Retrieves a bill and all its attributes from the database. Sets its ID to null and its date to now.
     * @param billToCloneId The ID of the bill to be retrieved
     * @return The retrieved and modified bill object
     */
    private fun generateClonedBill(billToCloneId: Long): Bill {
        val queryBillById = "SELECT * FROM ${BILL.tableName} WHERE ${BillEntry._ID.colName} = ?"

        this.retrieveBillsByQuery(queryBillById, arrayOf(billToCloneId.toString())).getOrNull(0)?.let { bill ->
            bill.id = null
            bill.date = Date()

            return bill
        }

        throw IllegalArgumentException("Invalid bill ID to clone")
    }

    /**
     * Retrieves a list of complete bill objects, with all the lines a persons, given a SQL query that retrieves
     * the corresponding rows only for the bill table.
     * @param query The SQL query which retrieves the corresponding records from the bill table
     * @param args Additional arguments for the query
     * @return The list of complete bill objects
     */
    private fun retrieveBillsByQuery(query: String, args: Array<String>): List<Bill> {
        val billsList = ArrayList<Bill>()

        this.dbHelper.readableDatabase.rawQuery(query, args).use { billCursor ->
            while(billCursor.moveToNext()) {
                val bill = SqlUtils.cursorToBill(billCursor)

                this.fillBillLines(bill)
                billsList.add(bill)
            }
        }

        return billsList
    }

    /**
     * Fills a bill object with its lines, by performing a query to the database
     * @param bill A bill with no lines, which will be filled
     */
    private fun fillBillLines(bill: Bill) {
        var queryLines = "SELECT * FROM %s WHERE %s = ?"
        queryLines = String.format(queryLines, BILL_LINE.tableName, BillLineEntry.BILL_ID.colName)

        this.dbHelper.readableDatabase.rawQuery(queryLines, arrayOf(bill.id.toString())).use { lineCursor ->
            while(lineCursor.moveToNext()) {
                val billLine = SqlUtils.cursorToBillLine(lineCursor, bill)

                this.fillBillLinePeople(billLine)
                bill.addLine(billLine)
            }
        }
    }

    /**
     * Fills a bill line object with its people, by performing a query to the database
     * @param billLine A bill line with no people, which will be filled
     */
    private fun fillBillLinePeople(billLine: BillLine) {
        var queryPersons = "SELECT * FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s = ?) ORDER BY %s DESC"
        queryPersons = String.format(queryPersons, PERSON.tableName, PersonEntry._ID.colName, BillLinePersonEntry.PERSON_ID.colName,
                BILL_LINE_PERSON.tableName, BillLinePersonEntry.BILL_LINE_ID.colName, PersonEntry.LAST_DATE.colName)

        this.dbHelper.readableDatabase.rawQuery(queryPersons, arrayOf(billLine.id.toString())).use { personCursor ->
            while (personCursor.moveToNext()) {
                billLine.assignPerson(SqlUtils.cursorToPerson(personCursor))
            }
        }
    }

    /**
     * Generates the content values object for a bill, which should be inserted in the bill table
     * @param bill The bill object
     * @return The content values
     */
    private fun generateBillValues(bill: Bill): ContentValues {
        val values = ContentValues()

        bill.id?.let { values.put(BillEntry._ID.colName, it) }

        values.put(BillEntry.NAME.colName, bill.name)
        values.put(BillEntry.TAX.colName, bill.tax)
        values.put(BillEntry.TIP_PERCENT.colName, bill.tipPercent)
        values.put(BillEntry.DATE.colName, bill.date.time)
        values.put(BillEntry.SOURCE.colName, bill.source.sourceId)
        values.put(BillEntry.STATUS.colName, bill.status.statusId)

        return values
    }

    /**
     * Generates the content values object for a bill line, which should be inserted in the bill line table
     * @param billId The ID of the bill the line belong to
     * @param billLine The bill line
     * @return The content values
     */
    private fun generateBillLineValues(billId: Long, billLine: BillLine): ContentValues {
        val values = ContentValues()

        values.put(BillLineEntry.BILL_ID.colName, billId)
        values.put(BillLineEntry.LINE_NUMBER.colName, billLine.lineNumber)
        values.put(BillLineEntry.NAME.colName, billLine.name)
        values.put(BillLineEntry.PRICE.colName, billLine.price)

        return values
    }

    /**
     * Generates the content values object for a person, which should be inserted in the person table
     * @param person The bill object
     * @return The content values
     */
    private fun generatePersonValues(person: Person): ContentValues {
        val values = ContentValues()

        values.put(PersonEntry.NAME.colName, person.name)
        values.put(PersonEntry.LAST_DATE.colName, person.lastDate.time)

        return values
    }

    /**
     * Generates the content values object for a bill line - person relationship, which should be inserted in the bill line - person relationship table
     * @param billLineId The ID of the bill line involved on the relationship
     * @param personId The ID of the person involved on the relationship
     * @return The content values
     */
    private fun generateBillLinePersonValues(billLineId: Long, personId: Long): ContentValues {
        val values = ContentValues()

        values.put(BillLinePersonEntry.BILL_LINE_ID.colName, billLineId)
        values.put(BillLinePersonEntry.PERSON_ID.colName, personId)

        return values
    }
}