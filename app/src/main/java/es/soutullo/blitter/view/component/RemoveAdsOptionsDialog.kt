package es.soutullo.blitter.view.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import es.soutullo.blitter.R
import es.soutullo.blitter.model.billing.RemoveAdsProductOption

object RemoveAdsOptionsDialog {
    fun show(
            context: Context,
            options: List<RemoveAdsProductOption>,
            titleResId: Int,
            messageResId: Int,
            onOptionSelected: (RemoveAdsProductOption) -> Unit,
            onDismissed: () -> Unit
    ): AlertDialog {
        lateinit var dialog: AlertDialog
        val content = createContent(context, options, messageResId) { option ->
            dialog.dismiss()
            onOptionSelected(option)
        }

        dialog = AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setView(content)
                .setNegativeButton(R.string.generic_dialog_cancel, null)
                .create()
                .apply {
                    setOnDismissListener { onDismissed() }
                    show()
                }

        return dialog
    }

    private fun createContent(
            context: Context,
            options: List<RemoveAdsProductOption>,
            messageResId: Int,
            onOptionSelected: (RemoveAdsProductOption) -> Unit
    ): View {
        val inflater = LayoutInflater.from(context)
        val contentParent = FrameLayout(context)
        val content = inflater.inflate(R.layout.dialog_remove_ads_options, contentParent, false)
        content.findViewById<TextView>(R.id.remove_ads_options_message).setText(messageResId)

        val optionsContainer = content.findViewById<LinearLayout>(R.id.remove_ads_options_container)

        options.forEach { option ->
            optionsContainer.addView(createOptionView(inflater, optionsContainer, option, onOptionSelected))
        }

        return content
    }

    private fun createOptionView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            option: RemoveAdsProductOption,
            onOptionSelected: (RemoveAdsProductOption) -> Unit
    ): View {
        return inflater.inflate(R.layout.item_remove_ads_option, parent, false).apply {
            findViewById<TextView>(R.id.remove_ads_option_name).text = option.displayName
            findViewById<TextView>(R.id.remove_ads_option_price).text = option.formattedPrice

            setOnClickListener { onOptionSelected(option) }
        }
    }
}
