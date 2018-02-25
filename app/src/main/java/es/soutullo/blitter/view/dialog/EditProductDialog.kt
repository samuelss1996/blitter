package es.soutullo.blitter.view.dialog

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.DialogEditProductBinding
import es.soutullo.blitter.view.adapter.data.ManualTranscriptionProduct
import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import es.soutullo.blitter.view.filter.InputFilterMinMax
import es.soutullo.blitter.view.util.BlitterUtils

/** The dialog shown when the user clicks the edit button on a product on the manual transcription activity */
class EditProductDialog(context: Context, handler: IDialogHandler, title: String, private val product: ManualTranscriptionProduct)
        : CustomLayoutDialog(context, handler, title) {

    /**
     * Returns the new product, whose attributes are given by the values on the fields of the dialog,
     * which the user can fill as he/she wants.
     * @return The new product. Null if the product can not be created because one or more fields have invalid values
     */
    fun getNewProduct(): ManualTranscriptionProduct? {
        val name = this.dialog.findViewById<EditText>(R.id.dialog_edit_product_name)?.text?.trim()?.toString()
        val price = this.dialog.findViewById<EditText>(R.id.dialog_edit_product_price)?.text?.toString()?.toDoubleOrNull()
        val quantity = this.dialog.findViewById<EditText>(R.id.dialog_edit_product_quantity)?.text?.toString()?.toIntOrNull()

        if(name != null && name.isNotBlank() && price != null && quantity != null) {
            return ManualTranscriptionProduct(name, price, quantity)
        }

        return null
    }

    override fun getCustomView(): View {
        val binding = DataBindingUtil.inflate<DialogEditProductBinding>(LayoutInflater.from(this.context), R.layout.dialog_edit_product, null, false)
        binding.utils = BlitterUtils

        with(binding.root) {
            val nameText = findViewById<EditText>(R.id.dialog_edit_product_name)

            nameText.setText(product.name)
            findViewById<EditText>(R.id.dialog_edit_product_price).setText(BlitterUtils.getEditablePriceAsString(product.unitPrice))
            findViewById<EditText>(R.id.dialog_edit_product_quantity).setText(product.quantity.toString())

            nameText.setSelection(nameText.length())
        }

        return binding.root
    }

    override fun onDialogCreated() {
        this.dialog.findViewById<EditText>(R.id.dialog_edit_product_quantity)?.filters = arrayOf(InputFilterMinMax(1, 50))
    }

    override fun preventDismissOnButtonClicked(): Boolean = true
    override fun getPositiveText(): String = this.context.getString(R.string.edit_product_dialog_positive_button)
    override fun getNegativeText(): String? = this.context.getString(R.string.generic_dialog_cancel)
}