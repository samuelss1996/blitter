package es.soutullo.blitter.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.ActivityFinalResultBinding
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.FinalResultAdapter
import es.soutullo.blitter.view.adapter.handler.IListHandler
import es.soutullo.blitter.view.dialog.ConfirmationDialog
import es.soutullo.blitter.view.dialog.PromptDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import es.soutullo.blitter.view.util.BillBitmapGenerator
import es.soutullo.blitter.view.util.BlitterUtils
import java.io.File
import java.io.FileOutputStream

class FinalResultActivity : AppCompatActivity(), IListHandler {
    private val peopleAdapter = FinalResultAdapter(this)

    private lateinit var binding: ActivityFinalResultBinding
    private lateinit var bill: Bill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_final_result)
        this.bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill

        this.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_final_result, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> this.onSupportNavigateUp()
            R.id.action_rename -> this.onRenameClicked()
            R.id.action_share -> this.onShareClicked()
            R.id.action_delete -> this.onDeleteClicked()
            R.id.action_clone -> this.onCloneClicked()
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
        val clickedPerson = this.peopleAdapter.get(listIndex)

        clickedPerson?.let {
            val intent = Intent(this@FinalResultActivity, BillPersonTraceActivity::class.java)

            intent.putExtra(BillPersonTraceActivity.PERSON_INTENT_DATA_KEY, it)
            intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.bill)
            this.startActivity(intent)
        }
    }

    /** Gets called when the "done" button at the bottom of the activity is clicked */
    fun onDoneClicked(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        this.startActivity(intent)
    }

    /** Gets called when the "modify" button at the bottom of the activity is clicked */
    fun onModifyClicked(view: View?) {
        val intent = Intent(this, BillSummaryActivity::class.java)

        intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.bill)
        this.startActivity(intent)
    }

    /** Gets called when the rename button on the app bar is clicked */
    private fun onRenameClicked() {
        val dialogTitle = this.getString(R.string.dialog_rename_bill_title)
        val negativeButtonText = this.getString(R.string.generic_dialog_cancel)
        val positiveButtonText = this.getString(R.string.action_activity_final_result_name)
        val editTextHint = this.getString(R.string.dialog_rename_bill_edit_text_hint)

        PromptDialog(this, this.createRenameDialogHandler(), dialogTitle, negativeButtonText,
                positiveButtonText, editTextHint, this.bill.name).show()
    }

    /** Gets called when the clone menu entry is clicked */
    private fun onCloneClicked() {
        this.bill.id?.let {
            DaoFactory.getFactory(this).getBillDao().cloneBill(it)
            this.onDoneClicked(null)
        }
    }

    /** Gets called when the share button on the app bar is clicked */
    private fun onShareClicked() {
        val contentUri = BillBitmapGenerator(this, this.bill, true).generateBillBitmap() // TODO change this
        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setDataAndType(contentUri, this.contentResolver.getType(contentUri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)

        this.startActivity(Intent.createChooser(shareIntent, "Choose an app")) // TODO change this
    }

    /** Gets called when the delete button on the app bar is clicked */
    private fun onDeleteClicked() {
        val title = this.resources.getQuantityString(R.plurals.dialog_delete_bill_title, 1)
        val message = this.resources.getQuantityString(R.plurals.dialog_delete_bill_message, 1)
        val positiveButtonText = this.getString(R.string.dialog_generic_delete_button)
        val negativeButtonText = this.getString(R.string.generic_dialog_cancel)

        ConfirmationDialog(this, this.createDeleteDialogHandler(), title, message,
                positiveButtonText, negativeButtonText).show()
    }

    /**
     * Gets called when a new name is assigned to the bill
     * @param newName The new name of the bill
     */
    private fun onRenamed(newName: String) {
        this.bill.name = newName
        this.supportActionBar?.title = newName
        this.doBackup()
    }

    /** Gets called when the deletion of the bill is confirmed */
    private fun onDeleteConfirmed() {
        this.bill.id?.let { DaoFactory.getFactory(this).getBillDao().deleteBills(listOf(it)) }
        this.onDoneClicked(null)
    }

    /** Saves the bill status on the database */
    private fun doBackup() {
        this.bill.status = EBillStatus.COMPLETED
        DaoFactory.getFactory(this).getBillDao().updateBill(this.bill.id, this.bill)
    }

    /** Initializes some fields of the activity */
    private fun init() {
        this.onRenamed(this.bill.name)
        this.binding.bill = this.bill

        this.findViewById<RecyclerView>(R.id.final_result_list).adapter = this.peopleAdapter

        this.peopleAdapter.add(null)
        this.peopleAdapter.addAll(this.bill.lines.map { it.persons }.flatten().distinct() )
    }

    /** Creates the dialog handler for the bill renaming dialog */
    private fun createRenameDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                (dialog as? PromptDialog)?.getUserInput()?.let { this@FinalResultActivity.onRenamed(it) }
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }

    /** Creates the dialog handler for the bill deletion confirmation dialog */
    private fun createDeleteDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                this@FinalResultActivity.onDeleteConfirmed()
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }
}
