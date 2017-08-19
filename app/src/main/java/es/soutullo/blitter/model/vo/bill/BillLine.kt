package es.soutullo.blitter.model.vo.bill

import es.soutullo.blitter.model.vo.person.BillPerson

/**
 *
 */
data class BillLine(val lineNumber: Int, val name: String, val price: Float, val persons: List<BillPerson>) {
    /**
     * @param person
     */
    fun assignPerson(person: BillPerson) {
        // TODO implement here
    }

    /**
     * @param person
     */
    fun unassignPerson(person: BillPerson) {
        // TODO implement here
    }

}