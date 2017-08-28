package es.soutullo.blitter.model.vo.bill

/** Represents a bill status. This status tells in which point of the splitting process the bill is.
 *  For example, if the user is still writing the products, the status will be [WRITING]*/
enum class EBillStatus(val statusId: Int) {
    WRITING(4),
    UNCONFIRMED(3),
    ASSIGNING(2),
    COMPLETED(1);

    companion object {

        /**
         * Finds the corresponding status given is numerical ID
         * @param statusId The numerical ID
         * @return The status
         */
        fun findStatusById(statusId: Int): EBillStatus = EBillStatus.values().find { it.statusId == statusId }!!
    }
}