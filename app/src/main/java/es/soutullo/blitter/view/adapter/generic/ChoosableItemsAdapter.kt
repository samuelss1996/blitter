package es.soutullo.blitter.view.adapter.generic

import android.support.v7.widget.RecyclerView
import android.view.View

import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

/**
 *
 */
abstract class ChoosableItemsAdapter<Item> : GenericListAdapter<Item>() {
    private val choosingModeEnabled: Boolean = false
    private val selectedIndexes: List<Int>? = null
    private val handler: IChoosableItemsListHandler? = null

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

    /**
     * @param view
     * @return
     */
    abstract override fun getViewHolder(view: View): RecyclerView.ViewHolder


    protected abstract inner class ChoosableItemViewHolder(itemView: View) : GenericListAdapter<Item>.GenericListViewHolder(itemView) {
        private val isChosen: Boolean = false

        /**
         * @param newState
         */
        protected abstract fun onChoiceStateChanged(newState: Boolean)

    }
}