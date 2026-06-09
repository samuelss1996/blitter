package es.soutullo.blitter.view.util

import android.app.Activity
import android.os.Build
import android.view.View

object EdgeToEdgeUtils {
    fun applySystemBarPadding(activity: Activity) {
        if (Build.VERSION.SDK_INT < 35) {
            return
        }

        val content = activity.findViewById<View>(android.R.id.content) ?: return
        val initialLeft = content.paddingLeft
        val initialTop = content.paddingTop
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom

        @Suppress("DEPRECATION")
        content.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                initialLeft + insets.systemWindowInsetLeft,
                initialTop + insets.systemWindowInsetTop,
                initialRight + insets.systemWindowInsetRight,
                initialBottom + insets.systemWindowInsetBottom
            )
            insets
        }
        content.requestApplyInsets()
    }
}
