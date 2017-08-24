package es.soutullo.blitter.view.dialog.generic

import android.content.Context
import es.soutullo.blitter.view.dialog.handler.IDialogHandler

/**
 *
 */
abstract class CustomLayoutDialog protected constructor(context: Context, handler: IDialogHandler, title: String) : CustomDialog(context, handler, title) {

    /**
     * @return
     */
    protected abstract fun getCustomView()
}