package es.soutullo.blitter.model.dao

import es.soutullo.blitter.model.vo.bill.Bill

/** Data access object for Bills */
interface BillDao {

    /**
     * Fetches all the bills from the database within the given range
     * @param begin The start index of the range
     * @param limit The quantity of bills to fetch
     * @return The list of bills
     */
    fun queryBills(begin: Int, limit: Int): List<Bill>

    /**
     * Performs a search of bills by their name. Finds all the bills whose name contain the given string
     * @param partialName The string to search for
     * @return The list of found bills
     */
    fun searchBills(partialName: String, limit: Int): List<Bill>

    /**
     * Stores a new bill on the database
     * @param bill The new bill to store
     * @return The auto-generated ID of the inserted bill
     */
    fun insertBill(bill: Bill): Long

    /**
     * Update a bill already present on the database
     * @param billId The ID of the bill that should be updated. If null, the bill is inserted as a new bill
     * @param bill The bill with its new values
     */
    fun updateBill(billId: Long? = null, bill: Bill)

    /**
     * Deletes a set of bills from the database
     * @param billsIds The ID's of the bills to delete
     */
    fun deleteBills(billsIds: List<Long>)

    /** Deletes all the bills from the database */
    fun deleteAllBills()

    /**
     * Clones the bill for reassigning. In other words, creates a new bill with the exact same attributes
     * as the given, except for the status, which is changed to [es.soutullo.blitter.model.vo.bill.EBillStatus.UNCONFIRMED].
     * @param billToCloneId The ID of the bill to be cloned
     * @return The new generated bill
     */
    fun cloneBillForReassigning(billToCloneId: Long): Bill

}