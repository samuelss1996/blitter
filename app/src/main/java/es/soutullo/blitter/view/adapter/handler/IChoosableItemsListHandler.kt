package es.soutullo.blitter.view.adapter.handler

/**
 *
 */
interface IChoosableItemsListHandler : IListHandler {

    /**
     *
     */
    fun onChoiceModeStarted()

    /**
     *
     */
    fun onChoiceModeFinished()

    /**
     *
     */
    fun onChosenItemsChanged()

}