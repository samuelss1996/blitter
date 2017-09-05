package es.soutullo.blitter.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import es.soutullo.blitter.R

class AssignationIntroActivity : AppIntro() {
    companion object {
            val FLAG_ASSIGNATION_INTRO_VIEWED = "FLAG_INTRO_ASSIGNATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.addSlide(AppIntroFragment.newInstance(this.getString(es.soutullo.blitter.R.string.assignation_intro_title),
                this.getString(es.soutullo.blitter.R.string.assignation_intro_description), R.drawable.ic_list_white_128dp,
                ContextCompat.getColor(this, R.color.md_green_600)))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.md_green_800)
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().putBoolean(FLAG_ASSIGNATION_INTRO_VIEWED, true).apply()

        val bill = this.intent.getSerializableExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY)
        this.startActivity(Intent(this, AssignationActivity::class.java).putExtra(BillSummaryActivity.BILL_INTENT_DATA_KEY, bill))
    }
}