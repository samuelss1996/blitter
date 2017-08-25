package es.soutullo.blitter.model.dao.sql.helper

import android.database.Cursor
import es.soutullo.blitter.model.BlitterSqlDbContract
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.bill.EBillSource
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.model.vo.person.Person
import java.util.*

object SqlUtils {

    /**
     * Converts a cursor positioned in a bill row to a bill object
     * @param cursor The cursor
     * @return The bill object
     */
    fun cursorToBill(cursor: Cursor) : Bill =  Bill (
            id = cursor.getLong(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry._ID.colName)),
            name = cursor.getString(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry.NAME.colName)),
            priceWithoutTip = cursor.getFloat(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry.PRICE_WITHOUT_TIP.colName)),
            tipPercent = cursor.getFloat(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry.TIP_PERCENT.colName)),
            date = Date(cursor.getLong(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry.DATE.colName))),
            source = EBillSource.findSourceById(cursor.getInt(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry.SOURCE.colName))),
            status = EBillStatus.findStatusById(cursor.getInt(cursor.getColumnIndex(BlitterSqlDbContract.BillEntry.STATUS.colName)))
    )

    /**
     * Converts a cursor positioned in a bill line row to a bill line object
     * @param cursor The cursor
     * @return The bill line object
     */
    fun cursorToBillLine(cursor: Cursor) : BillLine = BillLine (
            id = cursor.getLong(cursor.getColumnIndex(BlitterSqlDbContract.BillLineEntry._ID.colName)),
            lineNumber = cursor.getInt(cursor.getColumnIndex(BlitterSqlDbContract.BillLineEntry.LINE_NUMBER.colName)),
            name = cursor.getString(cursor.getColumnIndex(BlitterSqlDbContract.BillLineEntry.NAME.colName)),
            price = cursor.getFloat(cursor.getColumnIndex(BlitterSqlDbContract.BillLineEntry.PRICE.colName))
    )

    /**
     * Converts a cursor positioned in a person row to a person object
     * @param cursor The cursor
     * @return The person object
     */
    fun cursorToPerson(cursor: Cursor): Person = Person (
            id = cursor.getLong(cursor.getColumnIndex(BlitterSqlDbContract.PersonEntry._ID.colName)),
            name = cursor.getString(cursor.getColumnIndex(BlitterSqlDbContract.PersonEntry.NAME.colName)),
            lastDate = Date(cursor.getLong(cursor.getColumnIndex(BlitterSqlDbContract.PersonEntry.LAST_DATE.colName)))
    )
}