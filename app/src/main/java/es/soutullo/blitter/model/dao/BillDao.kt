package es.soutullo.blitter.model.dao

import es.soutullo.blitter.model.vo.bill.Bill

/**
 *
 */
interface BillDao {

    /**
     * @param begin
     * @param limit
     * @return
     */
    fun queryBills(begin: Int, limit: Int): List<Bill>

    /**
     * @param partialName
     * @return
     */
    fun searchBills(partialName: String): List<Bill>

    /**
     * @param bill
     * @return
     */
    fun insertBill(bill: Bill): Int

    /**
     * @param billId
     * @param bill
     */
    fun updateBill(billId: Int, bill: Bill)

    /**
     * @param billsIds
     */
    fun deleteBills(billsIds: List<Int>)

    /**
     * @param billToCloneId
     * @return
     */
    fun cloneBillForReassigning(billToCloneId: Int): Bill

}