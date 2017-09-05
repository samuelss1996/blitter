package es.soutullo.blitter.view.adapter

import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.view.adapter.generic.ChoosableItemsAdapter
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

/** RecyclerView adapter for the bill lines on the assignation activity */
class AssignationAdapter(handler: IChoosableItemsListHandler) : ChoosableItemsAdapter<BillLine>(handler) {
    override fun getItemLayout(): Int = R.layout.item_bill_line_assignation
}