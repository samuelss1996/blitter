package es.soutullo.blitter.view.dialog

import android.content.Context

import es.soutullo.blitter.view.dialog.generic.CustomLayoutDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 *
 */
class TipDialog(context: Context, handler: IDialogHandler, title: String, private val priceWithoutTip: Float) : CustomLayoutDialog(context, handler, title) {
    override fun getCustomView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPositiveText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareConcreteDialog(dialogBuilder: Builder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}