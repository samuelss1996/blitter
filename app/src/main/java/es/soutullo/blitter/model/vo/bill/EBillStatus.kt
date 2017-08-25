package es.soutullo.blitter.model.vo.bill

enum class EBillStatus(val statusId: Int) {
    WRITING(1),
    UNCONFIRMED(2),
    ASSIGNING(3),
    COMPLETED(4);

    companion object {
        fun findStatusById(statusId: Int): EBillStatus = EBillStatus.values().find { it.statusId == statusId }!!
    }
}