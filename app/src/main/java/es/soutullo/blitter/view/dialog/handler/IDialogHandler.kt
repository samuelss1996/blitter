package es.soutullo.blitter.view.dialog.handler

import es.soutullo.blitter.view.dialog.generic.CustomDialog

/**
 *
 */
interface IDialogHandler {

    /**
     * @param dialog
     */
    fun onPositiveButtonClicked(dialog: CustomDialog)

    /**
     * @param dialog
     */
    fun onNegativeButtonClicked(dialog: CustomDialog)

    /**
     * @param dialog
     */
    fun onNeutralButtonClicked(dialog: CustomDialog)

}