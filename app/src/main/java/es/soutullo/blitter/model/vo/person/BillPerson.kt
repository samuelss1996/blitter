package es.soutullo.blitter.model.vo.person

import es.soutullo.blitter.model.vo.bill.BillLine

/**
 *
 */
data class BillPerson(override val name: String, val lines: List<BillLine>) : Person(name) {

    fun getPayingAmountWithTip(): Float {
        TODO()
    }

    fun getCompletePayingAmountAsString(): String {
        TODO()
    }
}