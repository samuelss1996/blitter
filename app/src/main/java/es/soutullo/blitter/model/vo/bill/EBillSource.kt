package es.soutullo.blitter.model.vo.bill

enum class EBillSource(val sourceId: Int) {
    CAMERA(1),
    GALLERY(2),
    MANUAL(3);

    companion object {
        fun findSourceById(sourceId: Int): EBillSource = EBillSource.values().find { it.sourceId == sourceId }!!
    }
}