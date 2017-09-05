package es.soutullo.blitter.view.adapter.handler

/** Can be attached to any [es.soutullo.blitter.view.adapter.generic.ChoosableItemsAdapter] instance in
 *  order to listen for events produced on the recycler view */
interface IChoosableItemsListHandler : IListHandler {

    /** Gets called when the choice mode starts (i.e. when the user performs a long click over an item
     *  and after that he/she is able to pick more items */
    fun onChoiceModeStarted()

    /** Gets called when the choice mode finishes (e.g. when the user click the back button when he/she
     *  is in choice mode */
    fun onChoiceModeFinished()

    /** Gets called when the user selects or deselects one item. This includes the first selection, which
     *  occurs as a consequence of entering the choice mode */
    fun onChosenItemsChanged()

}