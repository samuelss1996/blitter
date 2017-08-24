package es.soutullo.blitter.model.dao.sql

import es.soutullo.blitter.model.dao.BillDao
import es.soutullo.blitter.model.vo.bill.Bill

/**
 *
 */
class SqlBillDao : BillDao {

    /**
     * @param begin
     * @param limit
     * @return
     */
    override fun queryBills(begin: Int, limit: Int): List<Bill> {
        // TODO implement here
        TODO("not implemented")
    }

    /**
     * @param partialName
     * @return
     */
    override fun searchBills(partialName: String): List<Bill> {
        // TODO implement here
        TODO("not implemented")
    }

    /**
     * @param bill
     * @return
     */
    override fun insertBill(bill: Bill): Int {
        // TODO implement here
        return 0
    }

    /**
     * @param billId
     * @param bill
     */
    override fun updateBill(billId: Int, bill: Bill) {
        // TODO implement here
    }

    /**
     * @param billsIds
     */
    override fun deleteBills(billsIds: List<Int>) {
        // TODO implement here
    }

    /**
     * @param billToCloneId
     * @return
     */
    override fun cloneBillForReassigning(billToCloneId: Int): Bill {
        // TODO implement here
        TODO("not implemented")
    }

}