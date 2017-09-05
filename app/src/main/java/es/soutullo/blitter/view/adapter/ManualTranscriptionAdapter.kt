package es.soutullo.blitter.view.adapter

import es.soutullo.blitter.R
import es.soutullo.blitter.view.adapter.data.ManualTranscriptionProduct
import es.soutullo.blitter.view.adapter.generic.GenericListAdapter

/** RecyclerView adapter for the products on the manual transcription activity */
class ManualTranscriptionAdapter : GenericListAdapter<ManualTranscriptionProduct>() {
    override fun getItemLayout(): Int = R.layout.item_product_manual_transcription_activity
    override fun clickableChildren(): Array<Int> = arrayOf(R.id.edit_product, R.id.delete_product)
}