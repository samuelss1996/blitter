package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityMainBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.RecentBillsAdapter
import io.github.kobakei.materialfabspeeddial.FabSpeedDial

// TODO hide FAB on all the lists activities when scroll down
// TODO lazy loading on the MainActivity
// TODO fix the "uncomplete" badge layout for the item bill (MainActivity)
class MainActivity : ChoosingLayoutActivity() {
    private lateinit var binding: ActivityMainBinding
    override val itemsAdapter = RecentBillsAdapter(this)

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
        this.binding.bills = recentBills
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
        this.itemsAdapter.finishChoiceMode()
        // TODO implement here
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
}
