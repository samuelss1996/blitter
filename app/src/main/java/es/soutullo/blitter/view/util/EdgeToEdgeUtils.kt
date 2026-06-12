package es.soutullo.blitter.view.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.util.WeakHashMap

@Suppress("DEPRECATION")
object EdgeToEdgeUtils {
    private const val STATUS_BAR_BACKGROUND_TAG = "blitter_status_bar_background"
    private val statusBarColors = WeakHashMap<Activity, Int>()

    fun applySystemBarPadding(activity: Activity, @ColorInt defaultStatusBarColor: Int = Color.TRANSPARENT) {
        if (Build.VERSION.SDK_INT < 35) {
            return
        }

        val content = activity.findViewById<View>(android.R.id.content) ?: return
        val decor = activity.window.decorView as? FrameLayout
        val initialPadding = content.paddingSnapshot()

        content.doOnApplyWindowInsets { view, insets ->
            view.setPadding(initialPadding.plusSystemInsets(insets))
        }

        decor?.applyStatusBarBackground {
            statusBarColors[activity] ?: defaultStatusBarColor
        }
    }

    fun applyStatusBarBackground(activity: Activity, @ColorInt color: Int) {
        setStatusBarColor(activity, color)

        if (Build.VERSION.SDK_INT < 35) {
            return
        }

        val decor = activity.window.decorView as? FrameLayout ?: return

        decor.applyStatusBarBackground {
            statusBarColors[activity] ?: color
        }
    }

    fun applyAppIntroInsets(activity: Activity, @ColorInt statusBarColor: Int) {
        setStatusBarColor(activity, statusBarColor)

        if (Build.VERSION.SDK_INT < 35) {
            return
        }

        val decor = activity.window.decorView as? FrameLayout ?: return
        decor.doOnApplyWindowInsets { _, insets ->
            val color = statusBarColors[activity] ?: statusBarColor

            decor.setStatusBarBackground(insets.statusBarInsetTop(), color)
        }
    }

    fun setStatusBarColorResource(activity: Activity, @ColorRes colorId: Int) {
        setStatusBarColor(activity, ContextCompat.getColor(activity, colorId))
    }

    fun setStatusBarColor(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        statusBarColors[activity] = color
        activity.window.statusBarColor = color

        if (Build.VERSION.SDK_INT >= 35) {
            val decor = activity.window.decorView as? FrameLayout ?: return
            val statusBarBackground = decor.findViewWithTag<View>(STATUS_BAR_BACKGROUND_TAG) ?: return

            statusBarBackground.setBackgroundColor(color)
        }
    }

    private fun FrameLayout.setStatusBarBackground(height: Int, @ColorInt color: Int) {
        if (height <= 0) {
            return
        }

        val statusBarBackground = this.findViewWithTag<View>(STATUS_BAR_BACKGROUND_TAG) ?: View(this.context).also { view ->
            view.tag = STATUS_BAR_BACKGROUND_TAG
            this.addView(
                view,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height, Gravity.TOP)
            )
        }

        statusBarBackground.layoutParams = (statusBarBackground.layoutParams as FrameLayout.LayoutParams).apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            this.height = height
            gravity = Gravity.TOP
        }
        statusBarBackground.setBackgroundColor(color)
        statusBarBackground.bringToFront()
    }

    private fun FrameLayout.applyStatusBarBackground(colorProvider: () -> Int) {
        this.doOnApplyWindowInsets { _, insets ->
            this.setStatusBarBackground(insets.statusBarInsetTop(), colorProvider())
        }
    }

    private fun View.doOnApplyWindowInsets(callback: (View, WindowInsets) -> Unit) {
        this.setOnApplyWindowInsetsListener { view, insets ->
            callback(view, insets)
            insets
        }
        this.requestApplyInsets()
    }

    private fun WindowInsets.statusBarInsetTop(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getInsets(WindowInsets.Type.statusBars()).top
        } else {
            this.systemWindowInsetTop
        }
    }

    private fun View.paddingSnapshot(): PaddingSnapshot {
        return PaddingSnapshot(this.paddingLeft, this.paddingTop, this.paddingRight, this.paddingBottom)
    }

    private fun View.setPadding(padding: PaddingSnapshot) {
        this.setPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    private fun PaddingSnapshot.plusSystemInsets(insets: WindowInsets): PaddingSnapshot {
        return copy(
            left = left + insets.systemWindowInsetLeft,
            top = top + insets.systemWindowInsetTop,
            right = right + insets.systemWindowInsetRight,
            bottom = bottom + insets.systemWindowInsetBottom
        )
    }

    private data class PaddingSnapshot(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
