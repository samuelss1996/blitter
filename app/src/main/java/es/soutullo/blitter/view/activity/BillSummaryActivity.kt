package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityBillSummaryBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.BillSummaryAdapter
import es.soutullo.blitter.view.util.BlitterUtils

class BillSummaryActivity : AppCompatActivity() {
    companion object {
        val BILL_INTENT_DATA_KEY = "EXTRA_BILL"
        val BILL_SEPARATOR_CHARS = arrayOf("-", "=", "*")
    }

    private lateinit var binding: ActivityBillSummaryBinding
    private lateinit var bill: Bill
    private lateinit var linesAdapter: BillSummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_bill_summary)
        this.bill = this.intent.getSerializableExtra(BILL_INTENT_DATA_KEY) as Bill
        this.linesAdapter = BillSummaryAdapter(this.bill.lines, this.assets)

        this.doBackup()
        this.init()
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    /** Gets called when the confirm (continue) button is clicked, in order to go to the next activity */
    fun onConfirmClicked(view: View) {
        val tutorialViewed = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(AssignationIntroActivity.FLAG_ASSIGNATION_INTRO_VIEWED, false)
        val intent = Intent(this, if(tutorialViewed) AssignationActivity::class.java else AssignationIntroActivity::class.java)

        intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.bill)
        this.startActivity(intent)
    }

    /** Gets called when the amend button is clicked, in order to modify the bill manually */
    fun onAmendClicked(view: View) {
        val intent = Intent(this, ManualTranscriptionActivity::class.java)

        intent.putExtra(BILL_INTENT_DATA_KEY, this.bill)
        this.startActivity(intent)
    }

    /** Saves the bill status on the database */
    private fun doBackup() {
        this.bill.status = EBillStatus.UNCONFIRMED
        DaoFactory.getFactory(this).getBillDao().updateBill(this.bill.id, this.bill)
    }

    /** Initializes some fields of the activity */
    private fun init() {
        val root = this.findViewById<ViewGroup>(R.id.root_bill_summary)

        this.binding.bill = this.bill
        this.binding.utils = BlitterUtils

        this.findViewById<RecyclerView>(R.id.bill_summary_lines_list).adapter = this.linesAdapter

        BlitterUtils.applyBillFontToChildren(root, this.assets)
        this.fillSeparators(root)
    }

    /**
     * Fills the bill separators with all the required chars in order to have as many chars as necessary
     * to fill the screen
     * @param root The root layout of this activity, which contains all the separators
     */
    private fun fillSeparators(root: ViewGroup) {
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                (0 until root.childCount).map { root.getChildAt(it) }.filterIsInstance<TextView>()
                        .filter { BILL_SEPARATOR_CHARS.contains(it.text) }
                        .associate {it to (root.measuredWidth / it.paint.measureText(it.text.toString())).toInt() - 2 }
                        .forEach { it.key.text = it.key.text.repeat(it.value) }
            }
        })
    }
}
