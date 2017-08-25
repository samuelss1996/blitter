package es.soutullo.blitter.model.vo.person

import es.soutullo.blitter.model.vo.bill.BillLine
import java.util.*
import kotlin.collections.ArrayList


data class Person(val id: Long?, val name: String, val lastDate: Date, val lines: List<BillLine> = ArrayList()) {

    fun getPayingAmountWithTip(): Float {
        TODO()
    }

    fun getCompletePayingAmountAsString(): String {
        TODO()
    }
}