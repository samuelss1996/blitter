package es.soutullo.blitter.view.dialog

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View

import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 *
 */
class TipDialog(context: Context, handler: IDialogHandler, title: String, private val priceWithoutTip: Float) : CustomLayoutDialog(context, handler, title) {
    override fun getCustomView(): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPositiveText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder) {
        super.prepareConcreteDialog(dialogBuilder)
    }
}