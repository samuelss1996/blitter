package es.soutullo.blitter.view.adapter

import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.view.adapter.generic.GenericListAdapter

class BillPersonTraceAdapter : GenericListAdapter<BillLine>() {
    override fun getItemLayout(): Int = R.layout.item_bill_trace_product
}