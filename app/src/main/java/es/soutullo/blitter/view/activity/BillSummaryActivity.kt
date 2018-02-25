package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityBillSummaryBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.BillSummaryAdapter
import es.soutullo.blitter.view.dialog.EditTaxDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
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

        this.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_activity_bill_summary, menu)
        return true
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> this.onSupportNavigateUp()
            R.id.action_edit_tax -> EditTaxDialog(this, this.createNewEditTaxDialogHandler(), this.bill.tax).show()
        }

        return true
    }

    /** Saves the bill status on the database */
    @Deprecated("It is not helpful to save the bill here, this method should not be used")
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
     * Changes the tax value of the bill. Gets called after the user changes this value
     * @param newValue The new tax value, specified by the user
     */
    private fun changeTaxValue(newValue: Double) {
        this.bill.tax = newValue
        this.binding.bill = this.bill

        this.binding.notifyChange()
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

    /** Creates the handler for the edit tax dialog, which allows the user to change the tax value of the bill */
    private fun createNewEditTaxDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                if(dialog is EditTaxDialog) {
                    this@BillSummaryActivity.changeTaxValue(dialog.getUserInput().toDoubleOrNull() ?: 0.0)
                }
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }
}
