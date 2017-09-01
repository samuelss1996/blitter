package es.soutullo.blitter.view.adapter.generic

import android.animation.LayoutTransition
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import es.soutullo.blitter.R
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

abstract class ChoosableItemsAdapter<Item>(choosableHandler: IChoosableItemsListHandler? = null) : GenericListAdapter<Item>(handler = choosableHandler) {
    var choosingModeEnabled = false
    val selectedIndexes = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericListViewHolder =
            ChoosableItemViewHolder(LayoutInflater.from(parent.context).inflate(this.getItemLayout(), parent, false))

    /** Should be called to start the choosing mode. Makes the checkboxes show up and allows the items to be selected */
    fun startChoiceMode() {
        this.setCheckboxesVisibility(View.VISIBLE)
        this.choosingModeEnabled = true

        (this.handler as? IChoosableItemsListHandler)?.onChoiceModeStarted()
    }

    /** Should be called to end the choosing mode. Deselects all the items and makes the checkboxes to hide */
    fun finishChoiceMode() {
        this.setCheckboxesVisibility(View.GONE)
        this.choosingModeEnabled = false
        this.deselectAll()

        (this.handler as? IChoosableItemsListHandler)?.onChoiceModeFinished()
    }

    /** Selects all the items */
    fun selectAll() {
        this.setNewStateToAll(true)
    }

    /** Deselects all the items */
    fun deselectAll() {
        this.setNewStateToAll(false)
    }

    /**
     * Change the choosing checkboxes viability
     * @param visibility The new visibility
     */
    private fun setCheckboxesVisibility(visibility: Int) {
        this.items.indices.map { this.recyclerView?.findViewHolderForAdapterPosition(it)?.itemView }
                .forEach { it?.findViewById<CheckBox>(R.id.choosing_checkbox)?.visibility = visibility }
    }

    /**
     * Selects/Deselects all the items
     * @param newState True if the items are supposed to be selected
     */
    private fun setNewStateToAll(newState: Boolean) {
        this.items.indices.map { this.recyclerView?.findViewHolderForAdapterPosition(it) }
                .filterIsInstance<ChoosableItemViewHolder>().forEach { it.changeState(newState) }
    }

    /** View holder for the choosable items */
    inner class ChoosableItemViewHolder(itemView: View) : GenericListAdapter<Item>.GenericListViewHolder(itemView) {
        private var itemCheckBox = this.view.findViewById<CheckBox>(R.id.choosing_checkbox)

        init {
            this.itemCheckBox.visibility = View.GONE

            (this.view as? ViewGroup)?.layoutTransition?.addTransitionListener(this.createOnTransitionListener())
            this.view.setOnLongClickListener { this.onLongClick() }
            this.itemCheckBox.setOnCheckedChangeListener {_, newState -> this.changeState(newState) }
        }

        override fun onClick(viewId: Int) {
            if (this@ChoosableItemsAdapter.choosingModeEnabled) {
                this.itemCheckBox.toggle()
            } else {
                super.onClick(viewId)
            }
        }

        /** Gets called when a long click is performed on the item */
        private fun onLongClick(): Boolean {
            if(!this@ChoosableItemsAdapter.choosingModeEnabled) {
                this@ChoosableItemsAdapter.startChoiceMode()
            }

            this.itemCheckBox.toggle()
            return true
        }

        /**
         * Changes the state of a item (the state can be select or unselected)
         * @param newState The new state
         */
        fun changeState(newState: Boolean) {
            this.itemCheckBox.isChecked = newState

            if(newState) {
                this@ChoosableItemsAdapter.selectedIndexes.add(this.adapterPosition)
                this.view.setBackgroundColor(ContextCompat.getColor(recyclerView?.context, R.color.md_grey_100))
            } else {
                this@ChoosableItemsAdapter.selectedIndexes.remove(this.adapterPosition)
                this.view.setBackgroundColor(ContextCompat.getColor(recyclerView?.context, R.color.md_white_1000))
            }

            (this@ChoosableItemsAdapter.handler as? IChoosableItemsListHandler)?.onChosenItemsChanged()
        }

        /**
         * Creates a transition listener for the transition performed when changing to or from choice mode.
         * Allows the items layout to be refreshed, which can be necessary for them to adapt to the new space
         * @return The listener
         */
        private fun createOnTransitionListener(): LayoutTransition.TransitionListener {
            return object : LayoutTransition.TransitionListener {
                override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {
                    notifyDataSetChanged()
                }

                override fun startTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) { }
            }
        }
    }
}