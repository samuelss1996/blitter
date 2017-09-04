package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
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

// TODO fix the "uncomplete" badge layout for the item bill (MainActivity)
class MainActivity : ChoosingLayoutActivity() {
    private lateinit var binding: ActivityMainBinding

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
        val recentBills = DaoFactory.getFactory(this).getBillDao().queryBills(0, 50)

        this.itemsAdapter.clear()
        this.itemsAdapter.addAll(recentBills)
        this.binding.bills = this.itemsAdapter.items
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (this.itemsAdapter.isChoosingModeEnabled()) {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_main_choosing, menu)
        } else {
            this.menuInflater.inflate(R.menu.menu_app_bar_activity_main, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_delete -> this.onDeleteClicked()
            R.id.action_settings -> this.startActivity(Intent(this, SettingsActivity::class.java))
        }

        return true
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        val bill = this.itemsAdapter.get(listIndex)
        val intent = when(bill.status) {
            EBillStatus.WRITING ->  Intent(this, ManualTranscriptionActivity::class.java)
            EBillStatus.UNCONFIRMED -> Intent(this, BillSummaryActivity::class.java)
            EBillStatus.ASSIGNING -> Intent(this, AssignationActivity::class.java)
            EBillStatus.COMPLETED -> Intent(this, FinalResultActivity::class.java)
        }

        intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, bill)
        this.startActivity(intent)
    }

    /** Gets called when the user clicks the manual transcription mini fab */
    private fun onManualTranscriptionClicked() {
        this.startActivity(Intent(this, ManualTranscriptionActivity::class.java))
    }

    /** Gets called when the user clicks the from gallery mini fab */
    private fun onFromGalleryClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the from camera mini fab */
    private fun onFromCameraClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the search button on the action bar */
    private fun onSearchClicked() {
        // TODO implement here
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

        DaoFactory.getFactory(this).getBillDao().deleteBills(selectedBills.mapNotNull { it.id })
        this.itemsAdapter.finishChoiceMode()

        Handler().postDelayed({
            val allSelected = (selectedBills.size == this.itemsAdapter.itemCount)

            this.itemsAdapter.items.removeAll(selectedBills)
            this.itemsAdapter.onLoadMore()
            this.binding.invalidateAll()

            if(this.itemsAdapter.itemCount > 0 && allSelected) {
                Toast.makeText(this, this.getString(R.string.on_all_bills_deleted_warning), Toast.LENGTH_LONG).show()
            }
        }, 500)
    }

    /**
     * Gets called when the text present on the search fields changes, due to a user interaction
     * @param newText The new text of the search field
     */
    private fun onSearchTextChanged(newText: String) {
        // TODO implement here
    }

    /** Initializes some fields of the activity */
    private fun init() {
        val fabSpeedDial = this.findViewById<FabSpeedDial>(R.id.fab)

        this.findViewById<RecyclerView>(R.id.recent_bills_list).adapter = this.itemsAdapter
        this.findViewById<CheckBox>(R.id.select_all_checkbox).setOnCheckedChangeListener(this.createCheckAllListener())

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
}
