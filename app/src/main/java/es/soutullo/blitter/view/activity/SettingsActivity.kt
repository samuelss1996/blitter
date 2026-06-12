package es.soutullo.blitter.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import es.soutullo.blitter.R
import es.soutullo.blitter.model.dao.DaoFactory
import es.soutullo.blitter.model.billing.AdsRemovalStore
import es.soutullo.blitter.model.billing.BillingProductKind
import es.soutullo.blitter.model.billing.RemoveAdsBillingError
import es.soutullo.blitter.model.billing.RemoveAdsBillingManager
import es.soutullo.blitter.model.billing.RemoveAdsProductCatalog
import es.soutullo.blitter.model.billing.RemoveAdsProductOption
import es.soutullo.blitter.view.activity.compat.AppCompatPreferenceActivity
import es.soutullo.blitter.view.component.RemoveAdsOptionsDialog
import es.soutullo.blitter.view.dialog.ConfirmationDialog
import es.soutullo.blitter.view.dialog.generic.CustomDialog
import es.soutullo.blitter.view.dialog.handler.IDialogHandler
import es.soutullo.blitter.view.util.EdgeToEdge
import es.soutullo.blitter.view.util.EdgeToEdgeMode
import es.soutullo.blitter.view.util.messageResId


@EdgeToEdge(mode = EdgeToEdgeMode.STATUS_BAR_ONLY)
class SettingsActivity : AppCompatPreferenceActivity() {
    private lateinit var billingManager: RemoveAdsBillingManager
    private lateinit var removeAdsPreference: Preference
    private var billingReady = false
    private var billingError = RemoveAdsBillingError.SERVICE_DISCONNECTED
    private var purchaseInProgress = false
    private var availableBillingOptions = listOf<RemoveAdsProductOption>()
    private var removeAdsOptionsDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.addPreferencesFromResource(R.xml.settings)

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.removeAdsPreference = this.findPreference(this.getString(R.string.preference_key_remove_ads))
        this.removeAdsPreference.setOnPreferenceClickListener { this.onRemoveAdsClicked() }
        this.findPreference(this.getString(R.string.preference_key_delete_all_bills)).setOnPreferenceClickListener { this.onDeleteAllBillsClicked() }
        this.findPreference(this.getString(R.string.preference_key_contact)).setOnPreferenceClickListener { this.onContactClicked() }

        this.prepareBilling()
        this.updateRemoveAdsPreference()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> this.finish()
        }

        return true
    }

    private fun onRemoveAdsClicked(): Boolean {
        val options = this.currentPurchaseOptions()
        if (!this.billingReady || options.isEmpty()) {
            this.billingManager.start()
            this.showBillingUnavailableToast(
                    if (this.billingReady) RemoveAdsBillingError.PRODUCT_UNAVAILABLE else this.billingError
            )
            return true
        }

        val adsAlreadyRemoved = AdsRemovalStore.areAdsRemoved(this)
        this.removeAdsOptionsDialog = RemoveAdsOptionsDialog.show(
                context = this,
                options = options,
                titleResId = if (adsAlreadyRemoved) R.string.extra_donation_options_title else R.string.remove_ads_options_title,
                messageResId = if (adsAlreadyRemoved) R.string.extra_donation_options_message else R.string.remove_ads_options_message,
                onOptionSelected = this::onRemoveAdsOptionSelected,
                onDismissed = { this.removeAdsOptionsDialog = null }
        )

        return true
    }

    private fun onRemoveAdsOptionSelected(option: RemoveAdsProductOption) {
        this.purchaseInProgress = true
        this.billingManager.launchPurchase(this, option.productId)
    }

    private fun prepareBilling() {
        this.billingManager = RemoveAdsBillingManager(
                context = this,
                removeAdsProductIds = RemoveAdsProductCatalog.removeAdsProductIds,
                supportProductIds = RemoveAdsProductCatalog.supportProductIds,
                listener = object : RemoveAdsBillingManager.Listener {
                    override fun onBillingReady(options: List<RemoveAdsProductOption>) {
                        this@SettingsActivity.billingReady = true
                        this@SettingsActivity.availableBillingOptions = options
                    }

                    override fun onBillingUnavailable(error: RemoveAdsBillingError) {
                        this@SettingsActivity.billingReady = false
                        this@SettingsActivity.billingError = error
                        this@SettingsActivity.availableBillingOptions = listOf()
                    }

                    override fun onPurchaseCompleted(kind: BillingProductKind) {
                        this@SettingsActivity.onPurchaseCompleted(kind)
                    }

                    override fun onPurchaseRestored() {
                        this@SettingsActivity.onPurchaseRestored()
                    }

                    override fun onPurchaseFailed(error: RemoveAdsBillingError) {
                        this@SettingsActivity.onPurchaseFailed(error)
                    }

                    override fun onPurchaseCancelled() {
                        this@SettingsActivity.restorePurchaseState()
                    }
                }
        )

        this.billingManager.start()
    }

    private fun onPurchaseCompleted(kind: BillingProductKind) {
        val messageResId = when (kind) {
            BillingProductKind.REMOVE_ADS -> R.string.toast_purchase_successful
            BillingProductKind.SUPPORT -> R.string.toast_extra_donation_successful
        }

        if (kind == BillingProductKind.REMOVE_ADS) {
            AdsRemovalStore.markAdsRemoved(this)
        }
        this.restorePurchaseState()
        this.updateRemoveAdsPreference()
        Toast.makeText(this, this.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun onPurchaseRestored() {
        val wasPurchaseInProgress = this.purchaseInProgress

        AdsRemovalStore.markAdsRemoved(this)
        this.restorePurchaseState()
        this.updateRemoveAdsPreference()

        if (wasPurchaseInProgress) {
            Toast.makeText(this, this.getString(R.string.toast_support_option_already_owned), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPurchaseFailed(error: RemoveAdsBillingError) {
        if (!this.purchaseInProgress) {
            return
        }

        this.restorePurchaseState()
        Toast.makeText(this, this.getString(error.messageResId()), Toast.LENGTH_SHORT).show()
    }

    private fun restorePurchaseState() {
        this.purchaseInProgress = false
    }

    private fun updateRemoveAdsPreference() {
        val adsRemoved = AdsRemovalStore.areAdsRemoved(this)

        this.removeAdsPreference.title = this.getString(
                if (adsRemoved) R.string.preference_title_extra_donation else R.string.preference_title_remove_ads
        )
        this.removeAdsPreference.summary = this.getString(
                if (adsRemoved) R.string.preference_summary_extra_donation else R.string.preference_summary_remove_ads
        )
    }

    private fun currentPurchaseOptions(): List<RemoveAdsProductOption> {
        val expectedKind = if (AdsRemovalStore.areAdsRemoved(this)) {
            BillingProductKind.SUPPORT
        } else {
            BillingProductKind.REMOVE_ADS
        }

        return this.availableBillingOptions.filter { it.kind == expectedKind }
    }

    private fun showBillingUnavailableToast(error: RemoveAdsBillingError) {
        Toast.makeText(this, this.getString(error.messageResId()), Toast.LENGTH_SHORT).show()
    }

    /** Gets called when the user clicks the delete all bills entry */
    private fun onDeleteAllBillsClicked(): Boolean {
        val title = this.getString(R.string.preference_title_delete_data)
        val message = this.getString(R.string.dialog_delete_all_bills_message)
        val positiveText = this.getString(R.string.dialog_generic_delete_button)
        val negativeText = this.getString(R.string.generic_dialog_cancel)

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

    override fun onDestroy() {
        this.removeAdsOptionsDialog?.dismiss()

        if (::billingManager.isInitialized) {
            this.billingManager.destroy()
        }

        super.onDestroy()
    }
}
