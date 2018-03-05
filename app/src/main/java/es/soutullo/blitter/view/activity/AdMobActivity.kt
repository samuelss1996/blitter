package es.soutullo.blitter.view.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import es.soutullo.blitter.R

class AdMobActivity : AppCompatActivity() {
    companion object {
        const val AD_MOB_APP_ID = "ca-app-pub-7211217453116351~6569738789"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_ad_mob)

        MobileAds.initialize(this, AD_MOB_APP_ID)
    }
}
