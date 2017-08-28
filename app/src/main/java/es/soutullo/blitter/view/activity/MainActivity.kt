package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityMainBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.RecentBillsAdapter
import es.soutullo.blitter.view.adapter.handler.IChoosableItemsListHandler
import io.github.kobakei.materialfabspeeddial.FabSpeedDial

class MainActivity : AppCompatActivity(), IChoosableItemsListHandler {
    private lateinit var binding: ActivityMainBinding
    private val recentBillsAdapter: RecentBillsAdapter = RecentBillsAdapter(this)

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

        this.recentBillsAdapter.clear()
        this.recentBillsAdapter.addAll(recentBills)
        this.binding.bills = recentBills
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_activity_main, menu)
        return true
    }

    override fun onChoiceModeStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChoiceModeFinished() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChosenItemsChanged() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        val bill = this.recentBillsAdapter.get(listIndex)
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
    fun onSearchClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the select all checkbox on the action bar */
    fun onSelectAllClicked() {
        // TODO implement here
    }

    /** Gets called when the user clicks the delete button on the action bar */
    fun onDeleteClicked() {
        // TODO implement here
    }

    /**
     * Gets called when the text present on the search fields changes, due to a user interaction
     * @param newText The new text of the search field
     */
    fun onSearchTextChanged(newText: String) {
        // TODO implement here
    }

    /** Initializes some fields of the activity */
    private fun init() {
        this.findViewById<RecyclerView>(R.id.recent_bills_list).adapter = this.recentBillsAdapter

        this.findViewById<FabSpeedDial>(R.id.fab).addOnMenuItemClickListener({ miniFab, label, itemId ->
            when(itemId) {
                R.id.fab_mini_transcribe -> this.onManualTranscriptionClicked()
                R.id.fab_mini_gallery -> this.onFromGalleryClicked()
                R.id.fab_mini_camera -> this.onFromCameraClicked()
            }
        })
    }
}
