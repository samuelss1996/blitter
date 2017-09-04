package es.soutullo.blitter.view.activity

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import es.soutullo.blitter.R
import es.soutullo.blitter.view.adapter.generic.ChoosableItemsAdapter
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler

/** Base class for any activity which implements the standard choosing layout on the app bar with recycler views */
abstract class ChoosingLayoutActivity: AppCompatActivity(), IChoosableItemsListHandler {
    abstract val itemsAdapter: ChoosableItemsAdapter<*>

    override fun onBackPressed() {
        if(this.itemsAdapter.isChoosingModeEnabled()) {
            this.itemsAdapter.finishChoiceMode()
        } else {
            super.onBackPressed()
        }
    }

    override fun onChoiceModeStarted() {
        this.changeBarLayout(true)
    }

    override fun onChoiceModeFinished() {
        this.changeBarLayout(false)
    }

    override fun onChosenItemsChanged() {
        val allCheckbox = this.findViewById<CheckBox>(R.id.select_all_checkbox)
        val checkAll = (this.itemsAdapter.itemCount == this.itemsAdapter.getSelectedIndexes().size)

        this.findViewById<TextView>(R.id.selected_items_count_text).text = this.itemsAdapter.getSelectedIndexes().size.toString()

        allCheckbox.setOnCheckedChangeListener(null)
        this.findViewById<CheckBox>(R.id.select_all_checkbox).isChecked = checkAll
        allCheckbox.setOnCheckedChangeListener(this.createCheckAllListener())
    }

    /** Creates the listener for the "select all" checkbox, displayed on the action bar in the choosing mode */
    protected fun createCheckAllListener(): CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener { _, checked ->
            if(checked) {
                this.itemsAdapter.selectAll()
            } else {
                this.itemsAdapter.deselectAll()
            }
        }
    }

    /**
     * Changes the bar layout and color depending on whether or not the activity is in choosing mode
     * @param choosingMode Indicates whether or not the activity is in choosing mode
     */
    private fun changeBarLayout(choosingMode: Boolean) {
        val appBarColorId = if(choosingMode) R.color.colorPrimaryLight else R.color.colorPrimary
        val statusBarColorId = if(choosingMode) R.color.colorPrimary else R.color.colorPrimaryDark

        this.invalidateOptionsMenu()
        this.supportActionBar?.setDisplayHomeAsUpEnabled(!choosingMode)
        this.findViewById<ViewGroup>(R.id.action_bar_content).visibility = if(choosingMode) View.VISIBLE else View.GONE

        this.supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,appBarColorId)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.window.statusBarColor = ContextCompat.getColor(this, statusBarColorId)
        }
    }
}