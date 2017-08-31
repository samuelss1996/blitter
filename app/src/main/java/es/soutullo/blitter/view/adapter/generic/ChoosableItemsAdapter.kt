package es.soutullo.blitter.view.adapter.generic

import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

abstract class ChoosableItemsAdapter<Item>(var choosableHandler: IChoosableItemsListHandler? = null) : GenericListAdapter<Item>(handler = choosableHandler) {
    private val choosingModeEnabled: Boolean = false
    private val selectedIndexes: List<Int>? = null

    fun startChoiceMode(firstSelectionIndex: Int) {
        // TODO implement here
    }

    fun finishChoiceMode() {
        // TODO implement here
    }

    fun selectAll() {
        // TODO implement here
    }

    fun deselectAll() {
        // TODO implement here
    }

    protected abstract fun choosingModeItemLayout(): Int

//    protected abstract inner class ChoosableItemViewHolder(itemView: View) : GenericListAdapter<Item>.GenericListViewHolder(itemView) {
//        private val isChosen: Boolean = false
//
//        /**
//         * @param newState
//         */
//        protected abstract fun onChoiceStateChanged(newState: Boolean)
//
//    }
}