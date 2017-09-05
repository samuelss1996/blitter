package es.soutullo.blitter.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import es.soutullo.blitter.R
import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler



class PromptDialog(context: Context, handler: IDialogHandler, title: String, private val negativeText: String,
                   private val positiveText: String, private val editTextTitle: String) : CustomLayoutDialog(context, handler, title) {
    private lateinit var view: View

    @SuppressLint("InflateParams")
    override fun getCustomView(): View {
        this.view = LayoutInflater.from(this.context).inflate(R.layout.dialog_prompt, null)
        this.view.findViewById<TextInputLayout>(R.id.text_input_layout).hint = this.editTextTitle

        return view
    }

    override fun onDialogCreated() {
        val promptEditText = this.dialog.findViewById<EditText>(R.id.dialog_prompt_edit_text)
        val inputMethod = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        promptEditText?.requestFocus()
        Handler().postDelayed({inputMethod.showSoftInput(promptEditText, InputMethodManager.SHOW_IMPLICIT)}, 100)
    }

    /** @return The user input on the EditText as a String */
    fun getUserInput(): String = this.view.findViewById<EditText>(R.id.dialog_prompt_edit_text).text.trim().toString()

    override fun getPositiveText(): String = this.positiveText
    override fun getNegativeText(): String? = this.negativeText
}