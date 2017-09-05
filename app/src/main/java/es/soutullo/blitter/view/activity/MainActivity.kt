package es.soutullo.blitter.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityMainBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.RecentBillsAdapter
import es.soutullo.blitter.view.dialog.ConfirmationDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import io.github.kobakei.materialfabspeeddial.FabSpeedDial

class MainActivity : ChoosingLayoutActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isSearchingMode: Boolean = false
    private var lastSearchTypedTime: Long = 0

    override val itemsAdapter = RecentBillsAdapter(this)
    override val showHomeAsUp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)

        this.init()
    }

    override fun onResume() {
        super.onResume()
        this.fetchRecentBills()
    }

    override fun onBackPressed() {
        if(this.isSearchingMode) {
            val choosingMode = this.itemsAdapter.isChoosingModeEnabled()

            if(this.itemsAdapter.isChoosingModeEnabled()) {
                super.onBackPressed()
            } else {
                this.fetchRecentBills()
            }

            this.showSearchOnBarLayout(choosingMode)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (this.itemsAdapter.isChoosingModeEnabled()) {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_main_choosing, menu)
        } else if(!this.isSearchingMode) {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_main, menu)

            if (this.itemsAdapter.isEmpty()) {
                menu?.removeItem(R.id.action_search)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_delete -> this.onDeleteClicked()
            R.id.action_settings -> this.startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_search -> this.onSearchClicked()
        }

        return true
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        this.itemsAdapter.get(listIndex)?.let { bill ->
            val intent = when(bill.status) {
                EBillStatus.WRITING ->  Intent(this, ManualTranscriptionActivity::class.java)
                EBillStatus.UNCONFIRMED -> Intent(this, BillSummaryActivity::class.java)
                EBillStatus.ASSIGNING -> Intent(this, AssignationActivity::class.java)
                EBillStatus.COMPLETED -> Intent(this, FinalResultActivity::class.java)
            }

            intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, bill)
            this.startActivity(intent)
        }
    }

    override fun onChoiceModeFinished() {
        super.onChoiceModeFinished()
        this.showSearchOnBarLayout(false)
    }

    /** Gets called when the user clicks the manual transcription mini fab */
    private fun onManualTranscriptionClicked() {
        this.startActivity(Intent(this, ManualTranscriptionActivity::class.java))
    }

    /** Gets called when the user clicks the from gallery mini fab */
    private fun onFromGalleryClicked() { }

    /** Gets called when the user clicks the from camera mini fab */
    private fun onFromCameraClicked() { }

    /** Gets called when the user clicks the search button on the action bar */
    private fun onSearchClicked() {
        this.showSearchOnBarLayout(true)
    }

    /** Gets called when the user clicks the delete button on the action bar */
    private fun onDeleteClicked() {
        val selectedBillsCount = this.itemsAdapter.getSelectedIndexes().size

        val title = this.resources.getQuantityString(R.plurals.dialog_delete_bill_title, selectedBillsCount)
        val message = this.resources.getQuantityString(R.plurals.dialog_delete_bill_message, selectedBillsCount, selectedBillsCount)
        val positiveText = this.getString(R.string.dialog_generic_delete_button)
        val negativeText = this.getString(R.string.dialog_generic_preserve_button)

        ConfirmationDialog(this, this.createDeleteDialogHandler(), title, message, positiveText, negativeText).show()
    }

    /** Gets called when the user confirms he/she wants to delete the selected bills */
    private fun onDeleteConfirmed() {
        val selectedBills = this.itemsAdapter.items.filterIndexed { index, _ -> this.itemsAdapter.getSelectedIndexes().contains(index) }

        DaoFactory.getFactory(this).getBillDao().deleteBills(selectedBills.mapNotNull { it?.id })
        this.itemsAdapter.finishChoiceMode()

        Handler().postDelayed({
            val allSelected = (selectedBills.size == this.itemsAdapter.itemCount)

            this.fetchRecentBills()

            if(this.itemsAdapter.itemCount > 0 && allSelected && !this.isSearchingMode) {
                Toast.makeText(this, this.getString(R.string.on_all_bills_deleted_warning), Toast.LENGTH_LONG).show()
            }
        }, 500)
    }

    /**
     * Gets called when the text present on the search fields changes, due to a user interaction
     * @param newText The new text of the search field
     */
    private fun onSearchTextChanged(newText: String) {
        val delay: Long = 500
        this.lastSearchTypedTime = System.currentTimeMillis()

        Handler().postDelayed({
            if(System.currentTimeMillis() - this.lastSearchTypedTime > delay && newText.trim() != "") {
                val results = DaoFactory.getFactory(this).getBillDao().searchBills(newText, 20)

                this.itemsAdapter.clear()
                this.itemsAdapter.addAll(if(results.isNotEmpty()) results else listOf(null))
            }
        }, delay + 50)
    }

    /** Fills the list with the most recent bills present on the database */
    private fun fetchRecentBills() {
        this.itemsAdapter.clear()
        this.itemsAdapter.onLoadMore()

        this.binding.invalidateAll()
        this.invalidateOptionsMenu()
    }

    /**
     * Changes the bar layout to search mode or standard mode
     * @param showSearch True if the layout should be search mode
     */
    private fun showSearchOnBarLayout(showSearch: Boolean) {
        this.isSearchingMode = showSearch

        val appBarColorId = if(showSearch) R.color.md_white_1000 else R.color.colorPrimary
        val statusBarColorId = if(showSearch) R.color.md_black_1000 else R.color.colorPrimaryDark


        this.supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,appBarColorId)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.window.statusBarColor = ContextCompat.getColor(this, statusBarColorId)
        }

        this.prepareSearchEditText(showSearch)
        this.invalidateOptionsMenu()
    }

    /**
     * Prepares the search edit text present on the app bar in searching mode
     * @param showSearch True if the layout is search mode
     */
    private fun prepareSearchEditText(showSearch: Boolean) {
        val editText = this.findViewById<EditText>(R.id.app_bar_search)

        editText.setText("")
        editText.visibility = if(showSearch) View.VISIBLE else View.GONE

        val inputMethod = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(showSearch) {
            editText.requestFocus()
            inputMethod.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } else {
            inputMethod.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    /** Initializes some fields of the activity */
    private fun init() {
        val fabSpeedDial = this.findViewById<FabSpeedDial>(R.id.fab)

        this.binding.bills = this.itemsAdapter.items
        this.findViewById<RecyclerView>(R.id.recent_bills_list).adapter = this.itemsAdapter
        this.findViewById<CheckBox>(R.id.select_all_checkbox).setOnCheckedChangeListener(this.createCheckAllListener())
        this.findViewById<EditText>(R.id.app_bar_search).addTextChangedListener(this.createSearchTextWatcher())

        this.itemsAdapter.fab = fabSpeedDial.mainFab
        fabSpeedDial.addOnMenuItemClickListener({ miniFab, label, itemId ->
            when(itemId) {
                R.id.fab_mini_transcribe -> this.onManualTranscriptionClicked()
                R.id.fab_mini_gallery -> this.onFromGalleryClicked()
                R.id.fab_mini_camera -> this.onFromCameraClicked()
            }
        })
    }

    /** @return The dialog handler for the bills deletion dialog */
    private fun createDeleteDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                this@MainActivity.onDeleteConfirmed()
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }

    /** Creates the TextWatcher for the search edit text, which manages the on text changed event */
    private fun createSearchTextWatcher(): TextWatcher {
        return object: TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                editable?.toString()?.let { this@MainActivity.onSearchTextChanged(it) }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        }
    }
}
