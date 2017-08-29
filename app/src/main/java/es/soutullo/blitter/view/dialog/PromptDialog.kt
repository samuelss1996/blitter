package es.soutullo.blitter.view.dialog

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

class PromptDialog(context: Context, handler: IDialogHandler, title: String, private val negativeText: String,
                   private val positiveText: String, private val editTextTitle: String) : CustomLayoutDialog(context, handler, title) {

    override fun getCustomView(): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPositiveText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNegativeText(): String? {
        TODO()
    }

    override fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder) {
        super.prepareConcreteDialog(dialogBuilder)
    }
}