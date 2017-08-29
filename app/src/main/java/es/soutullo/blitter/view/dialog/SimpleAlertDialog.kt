package es.soutullo.blitter.view.dialog

import android.content.Context
import android.support.v7.app.AlertDialog

import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

// TODO maybe replace some toasts with this
class SimpleAlertDialog(context: Context, handler: IDialogHandler, title: String, private val message: String) : CustomDialog(context, handler, title) {
    override fun getPositiveText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}