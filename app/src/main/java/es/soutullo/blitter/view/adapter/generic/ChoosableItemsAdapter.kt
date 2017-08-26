package es.soutullo.blitter.view.adapter.generic

import android.view.View

/**
 *
 */
abstract class ChoosableItemsAdapter<Item> : GenericListAdapter<Item>() {
    private val choosingModeEnabled: Boolean = false
    private val selectedIndexes: List<Int>? = null
    //private val handler: IChoosableItemsListHandler? = null

    /**
     * @param firstSelectionIndex
     */
    fun startChoiceMode(firstSelectionIndex: Int) {
        // TODO implement here
    }

    /**
     *
     */
    fun finishChoiceMode() {
        // TODO implement here
    }

    /**
     *
     */
    fun selectAll() {
        // TODO implement here
    }

    /**
     *
     */
    fun deselectAll() {
        // TODO implement here
    }

    /**
     * @return
     */
    protected abstract fun choosingModeItemLayout(): Int

    protected abstract inner class ChoosableItemViewHolder(itemView: View) : GenericListAdapter<Item>.GenericListViewHolder(itemView) {
        private val isChosen: Boolean = false

        /**
         * @param newState
         */
        protected abstract fun onChoiceStateChanged(newState: Boolean)

    }
}