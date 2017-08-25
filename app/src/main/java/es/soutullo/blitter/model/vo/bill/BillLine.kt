package es.soutullo.blitter.model.vo.bill

import es.soutullo.blitter.model.vo.person.Person

/**
 *
 */
data class BillLine(val id: Long?, val lineNumber: Int, val name: String, val price: Float, val persons: List<Person> = ArrayList()) {
    /**
     * @param person
     */
    fun assignPerson(person: Person) {
        // TODO implement here
    }

    /**
     * @param person
     */
    fun unassignPerson(person: Person) {
        // TODO implement here
    }

    fun getAssignedPeopleAsString() : String {
        TODO()
    }
}