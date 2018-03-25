package es.soutullo.blitter.model.dao

import es.soutullo.blitter.model.vo.person.Person

/** Data access object for persons */
interface PersonDao {

    /**
     * Retrieves the more recently used persons, based on their [Person.lastDate] attribute
     * @param limit The maximum number of persons to retrieve
     * @param exclude List of persons to exclude from the query
     * @return The list of persons
     */
    fun queryRecentPersons(limit: Int, exclude: List<Person>): List<Person>

    /**
     * Retrieves a person given its name.
     * @param name The exact name of the person
     * @return The found person
     */
    fun queryPersonByExactName(name: String): Person?

    /**
     * Inserts a new person, without assigning it to any bill or bill line
     * @param person The new person to insert
     */
    fun insertRecentPerson(person: Person)

    /**
     * Deletes a recent person from the recent persons list. The row is not actually deleted from the table
     * @param personName The name of the person to be deleted
     */
    fun deleteRecentPerson(personName: String)

    /** Deletes all the person from the database */
    fun deleteAllPersons()
}