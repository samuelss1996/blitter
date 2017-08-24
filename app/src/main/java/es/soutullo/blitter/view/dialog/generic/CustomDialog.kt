package es.soutullo.blitter.view.dialog.generic

import android.content.Context
import android.support.v7.app.AlertDialog

import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 *
 */
abstract class CustomDialog protected constructor(context: Context, private val handler: IDialogHandler, private val title: String) : AlertDialog(context) {

    /**
     *
     */
    fun showDialog() {
        // TODO implement here
    }

    /**
     * @return
     */
    protected fun preventDismissOnButtonClicked(): Boolean {
        // TODO implement here
        return false
    }

    open protected fun getNeutralText(): String? = null
    open protected fun getNegativeText(): String? = null

    protected abstract fun getPositiveText(): String

    /**
     * @param dialogBuilder
     */
    protected abstract fun prepareConcreteDialog(dialogBuilder: AlertDialog.Builder)

}