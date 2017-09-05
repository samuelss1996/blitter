package es.soutullo.blitter.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import es.soutullo.blitter.R
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.view.activity.compat.AppCompatPreferenceActivity
import es.soutullo.blitter.view.dialog.ConfirmationDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler


class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.addPreferencesFromResource(R.xml.settings)

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.findPreference(this.getString(R.string.preference_key_delete_all_bills)).setOnPreferenceClickListener { this.onDeleteAllBillsClicked() }
        this.findPreference(this.getString(R.string.preference_key_contact)).setOnPreferenceClickListener { this.onContactClicked() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> this.finish()
        }

        return true
    }

    /** Gets called when the user clicks the delete all bills entry */
    private fun onDeleteAllBillsClicked(): Boolean {
        val title = this.getString(R.string.preference_title_delete_bills)
        val message = this.getString(R.string.dialog_delete_all_bills_message)
        val positiveText = this.getString(R.string.dialog_generic_delete_button)
        val negativeText = this.getString(R.string.dialog_generic_preserve_button)

        ConfirmationDialog(this, this.createDeleteAllBillsDialogHandler(), title, message,
                positiveText, negativeText).show()

        return true
    }

    /** Gets called when the user clicks the contact entry */
    private fun onContactClicked(): Boolean {
        val address = this.getString(R.string.developer_email)
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null))

        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))

        this.startActivity(Intent.createChooser(emailIntent, this.getString(R.string.intent_chooser_title_send_mail)))

        return true
    }

    /** Gets called when the user confirms he/she wants to delete all the bills */
    private fun onDeleteAllBillsConfirmed() {
        DaoFactory.getFactory(this).getBillDao().deleteAllBills()
        DaoFactory.getFactory(this).getPersonDao().deleteAllPersons()

        Toast.makeText(this, this.getString(R.string.toast_delete_all_bills_success), Toast.LENGTH_SHORT).show()
    }

    /** Creates the dialog handler for the all bills deletion dialog */
    private fun createDeleteAllBillsDialogHandler(): IDialogHandler {
        return object : IDialogHandler {
            override fun onPositiveButtonClicked(dialog: CustomDialog) {
                this@SettingsActivity.onDeleteAllBillsConfirmed()
            }

            override fun onNegativeButtonClicked(dialog: CustomDialog) { }
            override fun onNeutralButtonClicked(dialog: CustomDialog) { }
        }
    }
}