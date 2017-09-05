package es.soutullo.blitter.view.adapter

import android.content.res.AssetManager
import android.view.ViewGroup
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.view.adapter.generic.GenericListAdapter
import es.soutullo.blitter.view.util.BlitterUtils

/** RecyclerView adapter for the bill lines on the bill summary activity */
class BillSummaryAdapter(items: MutableList<BillLine> = arrayListOf(), private val assets: AssetManager) : GenericListAdapter<BillLine>(items, null) {
    override fun getItemLayout(): Int = R.layout.item_bill_line_bill_summary
    override fun showSeparators(): Boolean = false

    override fun onBindViewHolder(holder: GenericListViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        BlitterUtils.applyBillFontToChildren(holder.itemView as ViewGroup, this.assets)
    }
}