package es.soutullo.blitter.view.activity

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.model.SliderPage

abstract class ABlitterIntroActivity : AppIntro() {
    protected abstract val titleId: Int
    protected abstract val descriptionId: Int
    protected abstract val drawableId: Int
    protected abstract val mainColorId: Int
    protected abstract val barColorId: Int
    protected abstract val preferenceKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppIntro 6 hides system bars by default; Blitter keeps the normal status bar visible.
        showStatusBar(true)
        setBarColor(ContextCompat.getColor(this, barColorId))
        addSlide(
            AppIntroFragment.createInstance(
                SliderPage(
                    title = getString(titleId),
                    description = getString(descriptionId),
                    imageDrawable = drawableId,
                    backgroundColorRes = mainColorId
                )
            )
        )
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(this.preferenceKey, true).apply()
    }
}
