package es.soutullo.blitter.view.adapter.generic

import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import es.soutullo.blitter.R
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

abstract class ChoosableItemsAdapter<Item>(choosableHandler: IChoosableItemsListHandler? = null) : GenericListAdapter<Item>(handler = choosableHandler) {
    private companion object {
        const val SELECTION_PAYLOAD = "selection"
    }

    private var choosingMode = false
    private val selectedIndexes = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericListViewHolder =
            ChoosableItemViewHolder(LayoutInflater.from(parent.context).inflate(this.getActualItemLayout(viewType), parent, false))

    override fun onBindViewHolder(holder: GenericListViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        (holder as? ChoosableItemViewHolder)?.bindSelectionState(position)
    }

    override fun onBindViewHolder(holder: GenericListViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.contains(SELECTION_PAYLOAD)) {
            (holder as? ChoosableItemViewHolder)?.bindSelectionState(position)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    /** @return True if the choosing mode is currently enabled */
    fun isChoosingModeEnabled() : Boolean = this.choosingMode

    /** @return The current selected indexes as as list of integers */
    fun getSelectedIndexes(): List<Int> = this.selectedIndexes.sorted()

    /** Ends the choice mode and returns to the normal mode */
    fun finishChoiceMode() {
        this.selectedIndexes.clear()
        this.choosingMode = false
        this.notifySelectionStateChanged()
        (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
        (this.handler as? IChoosableItemsListHandler)?.onChoiceModeFinished()
    }

    /** Selects all the items on the list while in choice mode */
    fun selectAll() {
        this.selectedIndexes.clear()
        this.items.indices
                .filter { this.items[it] != null }
                .forEach { this.selectedIndexes.add(it) }
        this.notifySelectionStateChanged()
        (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
    }

    /** Deselects all the items on the list while in choice mode */
    fun deselectAll() {
        this.selectedIndexes.clear()
        this.notifySelectionStateChanged()
        (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
    }

    private fun startChoiceMode(position: Int) {
        this.choosingMode = true
        this.selectedIndexes.add(position)
        this.notifySelectionStateChanged()
        (this.handler as? IChoosableItemsListHandler)?.onChoiceModeStarted()
        (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
    }

    private fun setItemSelected(position: Int, selected: Boolean) {
        if(position == RecyclerView.NO_POSITION || this.items.getOrNull(position) == null) {
            return
        }

        val selectionChanged = if(selected) {
            this.selectedIndexes.add(position)
        } else {
            this.selectedIndexes.remove(position)
        }

        if(selectionChanged) {
            this.notifyItemChanged(position, SELECTION_PAYLOAD)
            (this.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
        }
    }

    private fun notifySelectionStateChanged() {
        if(this.itemCount > 0) {
            this.notifyItemRangeChanged(0, this.itemCount, SELECTION_PAYLOAD)
        }
    }

    /** View holder for the choosable items */
    inner class ChoosableItemViewHolder(itemView: View) : GenericListAdapter<Item>.GenericListViewHolder(itemView) {
        private var isSelectable = false
        private var isActivated = false

        init {
            this.view.setOnLongClickListener { this.onLongClick() }
        }

        override fun onClick(viewId: Int) {
            val position = this.bindingAdapterPosition

            if(this@ChoosableItemsAdapter.choosingMode && this@ChoosableItemsAdapter.items.getOrNull(position) != null) {
                this@ChoosableItemsAdapter.setItemSelected(position, !this.isActivated)
            } else {
                super.onClick(viewId)
            }
        }

        /** Gets called when a long click is performed on the item */
        private fun onLongClick(): Boolean {
            val position = this.bindingAdapterPosition

            if(!this@ChoosableItemsAdapter.choosingMode && this@ChoosableItemsAdapter.items.getOrNull(position) != null) {
                this@ChoosableItemsAdapter.startChoiceMode(position)
                return true
            }

            return false
        }

        fun bindSelectionState(position: Int) {
            this.setActivated(this@ChoosableItemsAdapter.selectedIndexes.contains(position))
            this.setSelectable(this@ChoosableItemsAdapter.choosingMode)
        }

        /**
         * Called to select or deselect the item
         * @param activated True if the item should be selected
         */
        override fun setActivated(activated: Boolean) {
            this.isActivated = activated
            val background = if(activated) R.color.md_grey_100 else R.color.md_white_1000

            this.view.findViewById<CheckBox>(R.id.choosing_checkbox)?.let { checkbox ->
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = activated
            }
            this.view.setBackgroundColor(ContextCompat.getColor(this.view.context, background))
        }

        /**
         * Called to make the item selectable. In our case, a selectable item appears with an unchecked checkbox
         * on its left
         * @param selectable True if the item should be selectable
         */
        override fun setSelectable(selectable: Boolean) {
            this.isSelectable = selectable

            this.view.findViewById<CheckBox>(R.id.choosing_checkbox)?.let { checkbox ->
                checkbox.setOnCheckedChangeListener(null)
                checkbox.visibility = if(selectable) View.VISIBLE else View.GONE
                checkbox.setOnCheckedChangeListener { _, newState -> this@ChoosableItemsAdapter.setItemSelected(this.bindingAdapterPosition, newState) }
            }
        }

        /** @return True if the item is selectable */
        override fun isSelectable(): Boolean = this.isSelectable

        /** @return True if the item is selected */
        override fun isActivated(): Boolean = this.isActivated
    }
}
