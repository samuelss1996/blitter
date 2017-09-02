package es.soutullo.blitter.view.dialog.generic

import android.content.Context
import android.support.v7.app.AlertDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/** Generic class for custom dialogs */
abstract class CustomDialog protected constructor(protected val context: Context, private val handler: IDialogHandler?, private val title: String) {
    protected lateinit var dialog: AlertDialog

    /** Shows the dialog */
    fun show() {
        val builder = AlertDialog.Builder(this.context).setTitle(this.title).setPositiveButton(this.getPositiveText(), null)
                .setNegativeButton(this.getNegativeText(), null).setNeutralButton(this.getNeutralText(), null)

        this.prepareConcreteDialog(builder)
        this.dialog = builder.show()

        this.addListeners()
    }

    /** Closes the dialog */
    fun dismiss() {
        this.dialog.dismiss()
    }

    /** Adds the listeners to the dialog buttons. This listeners will call the corresponding methods of [handler] */
    private fun addListeners() {
        this.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{ this.maybeDismiss(); this.handler?.onPositiveButtonClicked(this) }
        this.dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{ this.maybeDismiss(); this.handler?.onNegativeButtonClicked(this) }
        this.dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener{ this.maybeDismiss(); this.handler?.onNeutralButtonClicked(this) }
    }

    /** Closes the dialog only if the [preventDismissOnButtonClicked] method returns false */
    private fun maybeDismiss() {
        if (!this.preventDismissOnButtonClicked()) {
            this.dialog.dismiss()
        }
    }

    /** @return True if the dialog should NOT be closed when any of its buttons is pressed */
    open protected fun preventDismissOnButtonClicked(): Boolean = false

    /** @return The text for the neutral button of the dialog. Null for no button */
    open protected fun getNeutralText(): String? = null

    /** @return The text for the negative button of the dialog. Null for no button */
    open protected fun getNegativeText(): String? = null

    /** @return The text for the positive button of the dialog */
    protected abstract fun getPositiveText(): String

    /**
     * Ables the implementing classes to perform extra custom operations against the dialog builder
     * @param dialogBuilder The dialog builder
     */
    protected abstract fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder)
}