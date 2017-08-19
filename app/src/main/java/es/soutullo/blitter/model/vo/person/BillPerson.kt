package es.soutullo.blitter.model.vo.person

import es.soutullo.blitter.model.vo.bill.BillLine

/**
 *
 */
data class BillPerson(val name: String, val lines: List<BillLine>) : Person(name) {

    /**
     * @return
     */
    // TODO implement here
    val payingAmountWithoutTip: Float
        get() = 0.0f

}