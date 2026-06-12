package es.soutullo.blitter.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import es.soutullo.blitter.R
import es.soutullo.blitter.model.billing.AdsRemovalStore
import es.soutullo.blitter.model.billing.BillingProductKind
import es.soutullo.blitter.model.billing.RemoveAdsBillingError
import es.soutullo.blitter.model.billing.RemoveAdsBillingManager
import es.soutullo.blitter.model.billing.RemoveAdsProductCatalog
import es.soutullo.blitter.model.billing.RemoveAdsProductOption
import es.soutullo.blitter.model.vo.bill.Bill
import es.soutullo.blitter.view.component.RemoveAdsOptionsDialog
import es.soutullo.blitter.view.util.messageResId

class AdMobActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AdMobActivity"
        private const val SECONDS_TO_WAIT = 5
    }

    private lateinit var bill: Bill
    private lateinit var billingManager: RemoveAdsBillingManager
    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = SECONDS_TO_WAIT
    private var adView: AdView? = null
    private var billingReady = false
    private var billingError = RemoveAdsBillingError.SERVICE_DISCONNECTED
    private var purchaseInProgress = false
    private var availableRemoveAdsOptions = listOf<RemoveAdsProductOption>()
    private var removeAdsOptionsDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_ad_mob)

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill
        this.prepareBilling()

        this.loadAds()
        this.waitSeconds()
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    fun onContinueClicked(view: View?) {
        val intent = Intent(this, FinalResultActivity::class.java)
        intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.bill)

        this.startActivity(intent)
    }

    fun onRemoveAdsClicked(view: View) {
        if (!this.billingReady) {
            this.billingManager.start()
            this.showBillingUnavailableToast()
            return
        }

        this.showRemoveAdsOptionsDialog()
    }

    private fun onRemoveAdsOptionSelected(option: RemoveAdsProductOption) {
        this.purchaseInProgress = true
        this.setRemoveAdsButtonEnabled(false)
        this.billingManager.launchPurchase(this, option.productId)
    }

    private fun loadAds() {
        MobileAds.initialize(this, this.getString(R.string.ad_mob_app_id))
        val adRequest = AdRequest.Builder().build()

        this.adView = this.findViewById<AdView>(R.id.adView).also { adView ->
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adView.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    Log.w(TAG, "Ad failed to load with code $errorCode")
                    adView.visibility = View.INVISIBLE
                }
            }
            adView.loadAd(adRequest)
        }
    }

    private fun onPurchaseFinished(showToast: Boolean) {
        this.purchaseInProgress = false

        if (showToast) {
            Toast.makeText(this, this.getString(R.string.toast_purchase_successful), Toast.LENGTH_SHORT).show()
        }

        AdsRemovalStore.markAdsRemoved(this)
        this.onContinueClicked(null)
    }

    private fun onPurchaseFailed(error: RemoveAdsBillingError) {
        if (!this.purchaseInProgress) {
            return
        }

        this.restoreRemoveAdsButton()
        Toast.makeText(this, this.getString(error.messageResId()), Toast.LENGTH_SHORT).show()
    }

    private fun onPurchaseCancelled() {
        this.restoreRemoveAdsButton()
    }

    private fun restoreRemoveAdsButton() {
        this.purchaseInProgress = false
        this.setRemoveAdsButtonEnabled(true)
    }

    private fun waitSeconds() {
        val button = this.findViewById<AppCompatButton>(R.id.ad_mob_continue_button)

        if(this.remainingSeconds == 0) {
            button.isEnabled = true
            button.text = this.getString(R.string.generic_dialog_continue)
        } else {
            button.text = this.resources.getQuantityString(R.plurals.button_text_wait_seconds, this.remainingSeconds, this.remainingSeconds)
            this.remainingSeconds--

            this.handler.postDelayed({ this.waitSeconds() }, 1000)
        }
    }

    private fun prepareBilling() {
        this.billingManager = RemoveAdsBillingManager(
                context = this,
                removeAdsProductIds = RemoveAdsProductCatalog.removeAdsProductIds,
                listener = object : RemoveAdsBillingManager.Listener {
                    override fun onBillingReady(options: List<RemoveAdsProductOption>) {
                        this@AdMobActivity.billingReady = true
                        this@AdMobActivity.availableRemoveAdsOptions = options
                        this@AdMobActivity.setRemoveAdsButtonEnabled(!this@AdMobActivity.purchaseInProgress)
                    }

                    override fun onBillingUnavailable(error: RemoveAdsBillingError) {
                        this@AdMobActivity.billingReady = false
                        this@AdMobActivity.billingError = error
                        this@AdMobActivity.availableRemoveAdsOptions = listOf()
                    }

                    override fun onPurchaseCompleted(kind: BillingProductKind) {
                        if (kind == BillingProductKind.REMOVE_ADS) {
                            this@AdMobActivity.onPurchaseFinished(showToast = true)
                        }
                    }

                    override fun onPurchaseRestored() {
                        this@AdMobActivity.onPurchaseFinished(showToast = false)
                    }

                    override fun onPurchaseFailed(error: RemoveAdsBillingError) {
                        this@AdMobActivity.onPurchaseFailed(error)
                    }

                    override fun onPurchaseCancelled() {
                        this@AdMobActivity.onPurchaseCancelled()
                    }
                }
        )

        this.billingManager.start()
    }

    private fun showRemoveAdsOptionsDialog() {
        if (this.availableRemoveAdsOptions.isEmpty()) {
            this.billingManager.start()
            this.showBillingUnavailableToast()
            return
        }

        this.removeAdsOptionsDialog = RemoveAdsOptionsDialog.show(
                context = this,
                options = this.availableRemoveAdsOptions,
                titleResId = R.string.remove_ads_options_title,
                messageResId = R.string.remove_ads_options_message,
                onOptionSelected = this::onRemoveAdsOptionSelected,
                onDismissed = { this.removeAdsOptionsDialog = null }
        )
    }

    private fun showBillingUnavailableToast() {
        Toast.makeText(this, this.getString(this.billingError.messageResId()), Toast.LENGTH_SHORT).show()
    }

    private fun setRemoveAdsButtonEnabled(enabled: Boolean) {
        this.findViewById<AppCompatButton>(R.id.ad_mob_remove_ads_button).isEnabled = enabled
    }

    override fun onResume() {
        super.onResume()
        this.adView?.resume()

        if (::billingManager.isInitialized && !this.purchaseInProgress) {
            this.billingManager.refreshPurchases()
        }
    }

    override fun onPause() {
        this.adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        this.handler.removeCallbacksAndMessages(null)
        this.removeAdsOptionsDialog?.dismiss()
        this.adView?.destroy()

        if (::billingManager.isInitialized) {
            this.billingManager.destroy()
        }

        super.onDestroy()
    }
}
