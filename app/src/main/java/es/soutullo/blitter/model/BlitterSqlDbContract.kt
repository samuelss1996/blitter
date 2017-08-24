package es.soutullo.blitter.model

object BlitterSqlDbContract {
    data class ForeignKey(val currentColumn: TableEntry, val refereeTable: Table, val refereeColumn: TableEntry)

    interface TableEntry {
        val colName: String
        val type: String
        val extraAttributes: String?
    }

    enum class Table(val tableName: String, val columns: Array<out TableEntry>, vararg val foreignKeys: ForeignKey)  {
        BILL("bill", BillEntry.values()),
        BILL_LINE("bill_line", BillLineEntry.values(), ForeignKey(BillLineEntry.BILL_ID, BILL , BillEntry._ID)),
        PERSON("person", PersonEntry.values()),
        BILL_PERSON("bill_person", BillPersonEntry.values(), ForeignKey(BillPersonEntry._ID, PERSON, PersonEntry._ID)),
        RECENT_PERSON("recent_person", RecentPersonEntry.values(), ForeignKey(RecentPersonEntry._ID, PERSON, PersonEntry._ID))
    }

    enum class BillEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        NAME("name", "TEXT"),
        PRICE_WITHOUT_TIP("price_without_tip", "REAL"),
        TIP_PERCENT("tip_percent", "REAL"),
        DATE("date", "INTEGER"),
        SOURCE("source", "INTEGER"),
        STATUS("status", "INTEGER")
    }

    enum class BillLineEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        BILL_ID("bill_id", "INTEGER"),
        LINE_NUMBER("line_number", "INTEGER"),
        NAME("name", "TEXT"),
        PRICE("price", "REAL")
    }

    enum class PersonEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        NAME("name", "TEXT", "NOT NULL UNIQUE"),
    }

    enum class BillPersonEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY")
    }

    enum class RecentPersonEntry(override val colName: String, override val type: String, override val extraAttributes: String? = null) : TableEntry {
        _ID("_id", "INTEGER", "PRIMARY KEY"),
        LAST_DATE("last_date", "INTEGER")
    }
}