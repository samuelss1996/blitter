package es.soutullo.blitter.model.dao

import es.soutullo.blitter.model.vo.person.Person

/** Data access object for persons */
interface PersonDao {

    /**
     * Retrieves the more recently used persons, based on their [Person.lastDate] attribute
     * @param limit The maximum number of persons to retrieve
     * @return The list of persons
     */
    fun queryRecentPersons(limit: Int): List<Person>
}