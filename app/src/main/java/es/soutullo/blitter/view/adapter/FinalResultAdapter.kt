package es.soutullo.blitter.view.adapter

import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.person.Person
import es.soutullo.blitter.view.adapter.generic.GenericListAdapter
import es.soutullo.blitter.view.adapter.handler.IListHandler

/** RecyclerView adapter for the persons list on the final result activity */
class FinalResultAdapter(handler: IListHandler) : GenericListAdapter<Person?>(handler = handler) {
    override fun getItemLayout(): Int = R.layout.item_person_final_result
    override fun getNullItemLayout(): Int = R.layout.item_tip_final_result
}