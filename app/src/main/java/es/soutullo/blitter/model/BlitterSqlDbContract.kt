package es.soutullo.blitter.model

/** SQL database contract. Contains the definition of the tables, including their columns and constraints */
object BlitterSqlDbContract {

    /**
     * Represents a foreign key relationship. One column refers to the column of other table
     * @param currentColumn The column where the constraint is added (i.e. the column which "points" to other column on other table)
     * @param refereeTable The target table of the constraint (i.e. the table which "is pointed")
     * @param refereeColumn The target column of the constraint (i.e. the column which "is pointed")
     */
    data class ForeignKey(val currentColumn: TableEntry, val refereeTable: Table, val refereeColumn: TableEntry)

    /** Interface that must be implemented by any enum which represents the columns of a table*/
    interface TableEntry {
        val colName: String
        val type: String
        val extraAttributes: String?
    }

    /**
     * Represents all the tables present on the database
     * @param tableName The name of each table
     * @param columns The array of columns of each table
     * @param foreignKeys The array of foreign keys constraints of each table
     */
    enum class Table(val tableName: String, val columns: Array<out TableEntry>, vararg val foreignKeys: ForeignKey)  {
        BILL("bill", BillEntry.values()),
        BILL_LINE("bill_line", BillLineEntry.values(), ForeignKey(BillLineEntry.BILL_ID, BILL, BillEntry._ID)),
        PERSON("person", PersonEntry.values()),
        BILL_LINE_PERSON("bill_line_person", BillLinePersonEntry.values(), ForeignKey(BillLinePersonEntry.BILL_LINE_ID, BILL_LINE, BillLineEntry._ID),
                ForeignKey(BillLinePersonEntry.PERSON_ID, PERSON, PersonEntry._ID))
    }

    /**
     * Represents all the columns for the bill table
     * @see TableEntry
     */
    enum class BillEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        NAME("name", "TEXT"),
        TIP_PERCENT("tip_percent", "REAL"),
        DATE("date", "INTEGER"),
        SOURCE("source", "INTEGER"),
        STATUS("status", "INTEGER")
    }

    /**
     * Represents all the columns for the bill line table
     * @see TableEntry
     */
    enum class BillLineEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        BILL_ID("bill_id", "INTEGER"),
        LINE_NUMBER("line_number", "INTEGER"),
        NAME("name", "TEXT"),
        PRICE("price", "REAL")
    }

    /**
     * Represents all the columns for the person table
     * @see TableEntry
     */
    enum class PersonEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        NAME("name", "TEXT", "NOT NULL UNIQUE"),
        LAST_DATE("last_date", "INTEGER")
    }

    /**
     * Represents all the columns for the bill line - person relationship table
     * @see TableEntry
     */
    enum class BillLinePersonEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        BILL_LINE_ID("bill_line_id", "INTEGER"),
        PERSON_ID("person_id", "INTEGER")
    }
}