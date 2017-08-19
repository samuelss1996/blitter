package es.soutullo.blitter.model.vo.bill

import java.util.*

/**
 *
 */
data class Bill(val id: Int, val name: String, val lines: List<BillLine>, val priceWithoutTip: Float,
                val tipPercent: Float, val date: Date, val source: EBillSource, val status: EBillStatus) {

    fun getLine(index: Int) {
        // TODO implement here
    }

    /**
     * @param line
     */
    fun addLine(line: BillLine) {
        // TODO implement here
    }
}