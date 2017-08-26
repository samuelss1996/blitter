package es.soutullo.blitter.view.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.model.vo.bill.BillLine
import es.soutullo.blitter.model.vo.bill.EBillSource
import es.soutullo.blitter.model.vo.bill.EBillStatus
import es.soutullo.blitter.view.adapter.ManualTranscriptionAdapter
import es.soutullo.blitter.view.adapter.data.ManualTranscriptionProduct
import es.soutullo.blitter.view.adapter.handler.IListHandler
import es.soutullo.blitter.view.dialog.ConfirmationDialog
import es.soutullo.blitter.view.dialog.EditProductDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import java.util.*

class ManualTranscriptionActivity : AppCompatActivity() {
    private val productsAdapter = ManualTranscriptionAdapter()
    private var billToAmend: Bill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_manual_transcription)

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.billToAmend = intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill?

        this.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_app_bar_activity_manual_transcription, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_done -> this.onFinishButtonClicked()
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    /** Gets called when the user clicks the add product button, present at the bottom of the screen */
    fun onAddProductButtonClicked(view: View?) {
        val productNameText = this.findViewById<EditText>(R.id.product_field)
        val productPriceText = this.findViewById<EditText>(R.id.product_price_field)

        if (productNameText.text.isNotBlank() && productPriceText.text.isNotBlank() && productPriceText.text.toString().toFloatOrNull() != null) {
            this.productsAdapter.add(ManualTranscriptionProduct(productNameText.text.trim().toString(), productPriceText.text.toString().toFloat(), 1))

            productNameText.setText("")
            productPriceText.setText("")
            productNameText.requestFocus()
        } else {
            this.onTryToAddProductWithWrongField()
        }
    }

    /** Gets called when the user clicks the finish button in the action bar */
    private fun onFinishButtonClicked() {
        if(!this.productsAdapter.isEmpty()) {
            val intent = Intent(this, BillSummaryActivity::class.java)

            intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.toBill(this.productsAdapter.items, EBillStatus.UNCONFIRMED))
            this.startActivity(intent)
        } else {
            this.onTryToFinishWithNoProducts()
        }
    }

    /**
     * Gets called when the user clicks the edit button for a product
     * @param listIndex The index of the product on the list of the adapter
     */
    private fun onEditProductClicked(listIndex: Int) {
        EditProductDialog(this, this.editDialogHandler(listIndex), this.getString(R.string.dialog_edit_product_title),
                this.productsAdapter.get(listIndex)).show()
    }

    /**
     * Gets called when the user clicks the delete button for a product
     * @param listIndex The index of the product on the list of the adapter
     */
    private fun onDeleteProductClicked(listIndex: Int) {
        ConfirmationDialog(this, this.deleteDialogHandler(listIndex), this.getString(R.string.dialog_delete_product_title),
                this.getString(R.string.dialog_delete_product_message, this.productsAdapter.get(listIndex).name),
                this.getString(R.string.dialog_delete_product_positive_button), this.getString(R.string.dialog_delete_product_negative_button))
            .show()
    }

    /**
     * Gets called when the user confirms he/she wants to delete a product, by clicking the proper button on a dialog
     * @param listIndex The index of the product on the list of the adapter
     */
    fun onDeleteProductConfirmed(listIndex: Int) {
        this.productsAdapter.remove(listIndex)
    }

    /** Gets called when the user presses the add/edit product button but he/she has not filled all the required fields properly */
    private fun onTryToAddProductWithWrongField() {
        Toast.makeText(this, this.getString(R.string.toast_manual_transcription_fill_fields), Toast.LENGTH_SHORT).show()
    }

    /** Gets called when the user presses the finish button but he/she has not added any product to the list */
    private fun onTryToFinishWithNoProducts() {
        Toast.makeText(this, this.getString(R.string.toast_manual_transcription_no_products), Toast.LENGTH_SHORT).show()
    }

    /**
     * Converts a list of manual transcription products to a bill, in order to be used by the next activities
     * of the app, or to be stored on the database
     * @param products The list of manual transcription products
     * @param status The status the new bill should have
     * @return The bill
     */
    private fun toBill(products: List<ManualTranscriptionProduct>, status: EBillStatus): Bill {
        val bill = Bill(null, this.getString(R.string.bill_uncompleted_default_name), Date(), EBillSource.MANUAL, status)

        var i = 0
        for ((name, unitPrice, quantity) in products) {
           for(j in 0 until quantity) {
               bill.addLine(BillLine(null, bill, i, name, unitPrice))
               i++
           }
        }

        return bill
    }

    /**
     * Performs the reverse operation of [toBill]. Converts a bill to a list of manual transcription products.
     * It's useful when the user clicks the amend button on the bill summary activity
     * @param nullableBill The bill, which might be null
     * @return The list of manual products. An empty list if the bill was null
     */
    private fun toProducts(nullableBill: Bill?): List<ManualTranscriptionProduct> {
        nullableBill?.let { bill ->
            return bill.lines.groupBy { Pair(it.name, it.price) }
                    .map { ManualTranscriptionProduct(it.value.first().name, it.value.first().price, it.value.size) }
        }

        return arrayListOf()
    }

    /** Initializes some fields of the activity */
    private fun init() {
        this.findViewById<EditText>(R.id.product_price_field).setOnEditorActionListener {_, _, _ -> this.onAddProductButtonClicked(null); true}
        this.findViewById<RecyclerView>(R.id.manual_products_list).adapter = this@ManualTranscriptionActivity.productsAdapter

        this.productsAdapter.addAll(this.toProducts(this.billToAmend))

        this.productsAdapter.handler = object : IListHandler {
            override fun onItemClicked(listIndex: Int, clickedViewId: Int) {
                when(clickedViewId) {
                    R.id.edit_product -> onEditProductClicked(listIndex)
                    R.id.delete_product -> onDeleteProductClicked(listIndex)
                }
            }
        }
    }

    /**
     * Creates the handler for the edit product dialog. This handler takes care of what to do when
     * the user clicks the buttons of the dialog
     * @param listIndex The index of the product being edited on the dialog, based on its position on
     * the adapter list
     * @return The handler
     */
    private fun editDialogHandler(listIndex: Int): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                if(dialog is EditProductDialog && dialog.getNewProduct() != null) {
                    this@ManualTranscriptionActivity.productsAdapter.update(listIndex, dialog.getNewProduct()!!)
                    dialog.dismiss()
                } else {
                    onTryToAddProductWithWrongField()
                }
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) {
                dialog.dismiss()
            }

            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }

    /**
     * Creates the handler for the delete product dialog. This handler takes care of what to do when
     * the user clicks the buttons of the dialog
     * @param listIndex The index of the product being edited on the dialog, based on its position on
     * the adapter list
     * @return The handler
     */
    private fun deleteDialogHandler(listIndex: Int): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                onDeleteProductConfirmed(listIndex)
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }
}
