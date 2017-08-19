package es.soutullo.blitter.view.dialog

import android.content.Context

import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 *
 */
class SimpleAlertDialog(context: Context, handler: IDialogHandler, title: String, private val message: String) : CustomDialog(context, handler, title) {
    override fun getPositiveText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareConcreteDialog(dialogBuilder: Builder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}