package es.soutullo.blitter.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import es.soutullo.blitter.R
import es.soutullo.blitter.databinding.DialogEditTaxBinding
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import es.soutullo.blitter.view.util.BlitterUtils

/** Dialog which allows the user to edit the tax value of the receipt */
class EditTaxDialog(context: Context, handler: IDialogHandler, private val currentTax: Double)
    : PromptDialog(context, handler, context.getString(R.string.dialog_edit_tax_title),
        context.getString(R.string.generic_dialog_cancel), context.getString(R.string.generic_dialog_positive_button), "") {

    @SuppressLint("InflateParams")
    override fun getCustomView(): View {
        val binding = DataBindingUtil.inflate<DialogEditTaxBinding>(LayoutInflater.from(this.context), R.layout.dialog_edit_tax, null, false)
        val editText = binding.root.findViewById<EditText>(R.id.dialog_edit_tax_edit_text)
        val clearButton = binding.root.findViewById<ImageButton>(R.id.dialog_edit_tax_clear)

        this.view = binding.root
        binding.utils = BlitterUtils

        editText.setText(BlitterUtils.getEditablePriceAsString(this.currentTax))
        clearButton.setOnClickListener { editText.setText("") }
        editText.selectAll()

        return binding.root
    }

    override fun getDialogEditText() = R.id.dialog_edit_tax_edit_text
}