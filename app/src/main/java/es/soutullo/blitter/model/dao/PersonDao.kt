package es.soutullo.blitter.model.dao

import es.soutullo.blitter.model.vo.person.Person

/**
 *
 */
interface PersonDao {

    /**
     * @return
     */
    fun queryRecentPersons(): List<Person>

}