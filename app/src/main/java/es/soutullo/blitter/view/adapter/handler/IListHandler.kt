package es.soutullo.blitter.view.adapter.handler

/** Can be attached to any [es.soutullo.blitter.view.adapter.generic.GenericListAdapter] instance in
 *  order to listen for events produced on the recycler view */
interface IListHandler {

    /**
     * Gets called when an item of a recycler view is clicked. Gets also called if the click is performed
     * in a view contained in an item of a recycler view which is marked as a clickable child
     * @param listIndex The index of the clicked item on the recycler view
     * @param clickedViewId The ID of the clicked view
     */
    fun onItemClicked(listIndex: Int, clickedViewId: Int)
}