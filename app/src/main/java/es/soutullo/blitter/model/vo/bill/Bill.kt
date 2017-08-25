package es.soutullo.blitter.model.vo.bill

import java.util.*

/**
 *
 */
data class Bill(var id: Long?, val name: String, val date: Date, val source: EBillSource, val status: EBillStatus,
                val lines: List<BillLine> = ArrayList(), val priceWithoutTip: Float = 0f, val tipPercent: Float = 0f) {

    fun getLine(index: Int) {
        // TODO implement here
    }

    /**
     * @param line
     */
    fun addLine(line: BillLine) {
        // TODO implement here
    }

    fun getBeautifulPrice(): String {
        TODO()
    }

    fun getBeautifulDate(): String {
        TODO()
    }
}