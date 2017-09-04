package es.soutullo.blitter.view.adapter

import es.soutullo.blitter.R
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.view.adapter.generic.ChoosableItemsAdapter
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

class RecentBillsAdapter(handler: IChoosableItemsListHandler) : ChoosableItemsAdapter<Bill>(handler) {
    override fun getItemLayout(): Int = R.layout.item_bill_main_activity

    override fun onLoadMore() {
        this.recyclerView?.context?.let {
            val newBills = DaoFactory.getFactory(it).getBillDao().queryBills(this.itemCount, 50)
            this.items.addAll(newBills)

            this.notifyDataSetChanged()
        }
    }
}