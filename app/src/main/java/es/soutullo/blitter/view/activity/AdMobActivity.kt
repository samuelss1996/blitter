package es.soutullo.blitter.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import es.soutullo.blitter.R
import es.soutullo.blitter.model.billing.IabHelper
import es.soutullo.blitter.model.billing.IabResult
import es.soutullo.blitter.model.billing.Purchase
import es.soutullo.blitter.model.vo.bill.Bill

class AdMobActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AdMobActivity"
        private const val SECONDS_TO_WAIT = 5
        private const val PURCHASE_RETURN_CODE_ID = 10
    }

    private lateinit var bill: Bill
    private lateinit var billingHelper: IabHelper
    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = SECONDS_TO_WAIT
    private var adView: AdView? = null

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
        if (!this.billingHelper.asyncInProgress) {
            this.billingHelper.launchPurchaseFlow(this, this.getString(R.string.sku_remove_ads), PURCHASE_RETURN_CODE_ID,
                    { result, purchase -> this.onPurchaseFinished(result, purchase) }, "")
        }
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

    private fun onPurchaseFinished(result: IabResult, purchase: Purchase?) {
        if(result.isSuccess && purchase != null && purchase.sku == this.getString(R.string.sku_remove_ads)) {
            Toast.makeText(this, this.getString(R.string.toast_purchase_successful), Toast.LENGTH_SHORT).show()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(this.getString(R.string.preference_key_ads_removed), true).apply()

            this.onContinueClicked(null)
        } else {
            Toast.makeText(this, this.getString(R.string.toast_purchase_error), Toast.LENGTH_SHORT).show()
        }
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
        this.billingHelper = IabHelper(this, this.getString(R.string.billing_public_key))

        this.billingHelper.startSetup { result ->
            if(result.isSuccess) {
                val removeAdsButton = this.findViewById<AppCompatButton>(R.id.ad_mob_remove_ads_button)

                removeAdsButton.isEnabled = true
                removeAdsButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (this.billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        this.adView?.resume()
    }

    override fun onPause() {
        this.adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        this.handler.removeCallbacksAndMessages(null)
        this.adView?.destroy()
        super.onDestroy()
        this.billingHelper.dispose()
    }
}
