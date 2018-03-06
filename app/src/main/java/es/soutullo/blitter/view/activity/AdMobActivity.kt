package es.soutullo.blitter.view.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.AppCompatButton
import android.text.format.DateFormat
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import es.soutullo.blitter.R
import es.soutullo.blitter.model.vo.bill.Bill
import java.util.*

class AdMobActivity : AppCompatActivity() {
    companion object {
        private const val SECONDS_TO_WAIT = 5
        private const val AD_MOB_APP_ID = "ca-app-pub-7211217453116351~6569738789"
    }

    private lateinit var bill: Bill
    private var remainingSeconds = SECONDS_TO_WAIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_ad_mob)

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY) as Bill

        this.loadAds()
        this.waitSeconds()
    }

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    fun onContinueClicked(view: View) {
        val intent = Intent(this, FinalResultActivity::class.java)
        intent.putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, this.bill)

        this.bill.name = this.getString(R.string.bill_final_name_pattern, DateFormat.getDateFormat(this).format(Date()))
        this.startActivity(intent)
    }

    private fun loadAds() {
        MobileAds.initialize(this, AD_MOB_APP_ID)
        val adRequest = AdRequest.Builder().build()
        val adView = this.findViewById<AdView>(R.id.adView)

        adView.loadAd(adRequest)
    }

    private fun waitSeconds() {
        val button = this.findViewById<AppCompatButton>(R.id.ad_mob_continue_button)

        if(this.remainingSeconds == 0) {
            button.isEnabled = true
            button.text = this.getString(R.string.generic_dialog_continue)
        } else {
            button.text = this.resources.getQuantityString(R.plurals.button_text_wait_seconds, this.remainingSeconds, this.remainingSeconds)
            this.remainingSeconds--

            Handler().postDelayed({ this.waitSeconds() }, 1000)
        }
    }
}
