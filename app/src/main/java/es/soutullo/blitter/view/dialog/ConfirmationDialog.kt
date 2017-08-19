package es.soutullo.blitter.view.dialog

import android.content.Context
import android.support.v7.app.AlertDialog

import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 *
 */
class ConfirmationDialog(context: Context, handler: IDialogHandler, title: String, private val message: String,
                         private val positiveText: String, private val negativeText: String) : CustomDialog(context, handler, title) {

    /**
     * @return
     */
    override fun getPositiveText(): String = this.positiveText

    override fun getNegativeText(): String? = this.negativeText

    /**
     * @param dialogBuilder
     */
    override fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder) {

    }

}