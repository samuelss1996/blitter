package es.soutullo.blitter.model.vo.bill

/** Represents a bill source, in other words, which input method followed the user to store the bill
 *  into the app's database. Examples could be: from camera, gallery or manually */
enum class EBillSource(val sourceId: Int) {
    CAMERA(1),
    GALLERY(2),
    MANUAL(3);

    companion object {

        /**
         * Finds the corresponding source given is numerical ID
         * @param sourceId The numerical ID
         * @return The source
         */
        fun findSourceById(sourceId: Int): EBillSource = EBillSource.values().find { it.sourceId == sourceId }!!
    }
}