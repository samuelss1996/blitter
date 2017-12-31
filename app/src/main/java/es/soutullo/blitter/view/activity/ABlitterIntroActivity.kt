package es.soutullo.blitter.view.activity

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment

abstract class ABlitterIntroActivity : AppIntro() {
    protected abstract val titleId: Int
    protected abstract val descriptionId: Int
    protected abstract val drawableId: Int
    protected abstract val mainColorId: Int
    protected abstract val barColorId: Int
    protected abstract val preferenceKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.addSlide(AppIntroFragment.newInstance(this.getString(this.titleId), this.getString(this.descriptionId), this.drawableId,
                ContextCompat.getColor(this, this.mainColorId)))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.window.statusBarColor = ContextCompat.getColor(this, this.barColorId)
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(this.preferenceKey, true).apply()
    }
}