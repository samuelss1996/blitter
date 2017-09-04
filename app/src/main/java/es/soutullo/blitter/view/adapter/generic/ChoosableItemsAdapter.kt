package es.soutullo.blitter.view.adapter.generic

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.bignerdranch.android.multiselector.MultiSelector
import es.soutullo.blitter.R
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

abstract class ChoosableItemsAdapter<Item>(choosableHandler: IChoosableItemsListHandler? = null) : GenericListAdapter<Item>(handler = choosableHandler) {
    private val multiSelector = MultiSelector()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericListViewHolder =
            ChoosableItemViewHolder(LayoutInflater.from(parent.context).inflate(this.getActualItemLayout(viewType), parent, false))

    /** @return True if the choosing mode is currently enabled */
    fun isChoosingModeEnabled() : Boolean = this.multiSelector.isSelectable

    /** @return The current selected indexes as as list of integers */
    fun getSelectedIndexes(): List<Int> = this.multiSelector.selectedPositions

    /** Ends the choice mode and returns to the normal mode */
    fun finishChoiceMode() {
        this.deselectAll()

        this.multiSelector.isSelectable = false
        (this.handler as? IChoosableItemsListHandler)?.onChoiceModeFinished()
    }

    /** Selects all the items on the list while in choice mode */
    fun selectAll() {
        this.items.indices.forEach { this.multiSelector.setSelected(it, 0, true) }
        (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
    }

    /** Deselects all the items on the list while in choice mode */
    fun deselectAll() {
        this.items.indices.forEach { this.multiSelector.setSelected(it, 0, false) }
        (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
    }

    /** View holder for the choosable items */
    inner class ChoosableItemViewHolder(itemView: View) : GenericListAdapter<Item>.GenericListViewHolder(itemView, this.multiSelector) {
        private var isSelectable = false
        private var isActivated = false

        init {
            this.view.setOnLongClickListener { this.onLongClick() }
        }

        override fun onClick(viewId: Int) {
            if(this@ChoosableItemsAdapter.multiSelector.isSelectable) {
                this@ChoosableItemsAdapter.multiSelector.setSelected(this, !this.isActivated)
            } else {
                super.onClick(viewId)
            }
        }

        /** Gets called when a long click is performed on the item */
        private fun onLongClick(): Boolean {
            if(!this@ChoosableItemsAdapter.multiSelector.isSelectable) {
                (this@ChoosableItemsAdapter.handler as? IChoosableItemsListHandler)?.onChoiceModeStarted()

                this@ChoosableItemsAdapter.multiSelector.isSelectable = true
                this@ChoosableItemsAdapter.multiSelector.setSelected(this, true)

                return true
            }

            return false
        }

        /**
         * Called to select or deselect the item
         * @param activated True if the item should be selected
         */
        override fun setActivated(activated: Boolean) {
            this.isActivated = activated
            val background = if(activated) R.color.md_grey_100 else R.color.md_white_1000

            (this@ChoosableItemsAdapter.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()

            this.view.findViewById<CheckBox>(R.id.choosing_checkbox).isChecked = activated
            this.view.setBackgroundColor(ContextCompat.getColor(this@ChoosableItemsAdapter.recyclerView?.context, background))
        }

        /**
         * Called to make the item selectable. In our case, a selectable item appears with an unchecked checkbox
         * on its left
         * @param selectable True if the item should be selectable
         */
        override fun setSelectable(selectable: Boolean) {
            this.isSelectable = selectable

            with(this.view.findViewById<CheckBox>(R.id.choosing_checkbox)) {
                this.visibility = if(selectable) View.VISIBLE else View.GONE
                this.setOnCheckedChangeListener({ _, newState -> this@ChoosableItemsAdapter.multiSelector.setSelected(this@ChoosableItemViewHolder, newState) })
            }
        }

        /** @return True if the item is selectable */
        override fun isSelectable(): Boolean = this.isSelectable

        /** @return True if the item is selected */
        override fun isActivated(): Boolean = this.isActivated
    }
}