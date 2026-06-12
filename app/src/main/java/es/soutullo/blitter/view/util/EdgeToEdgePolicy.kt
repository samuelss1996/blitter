package es.soutullo.blitter.view.util

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import es.soutullo.blitter.R

object EdgeToEdgePolicy {
    fun apply(activity: Activity) {
        val edgeToEdge = activity.javaClass.getAnnotation(EdgeToEdge::class.java)
        val mode = edgeToEdge?.mode ?: EdgeToEdgeMode.CONTENT

        when (mode) {
            EdgeToEdgeMode.CONTENT -> EdgeToEdgeUtils.applySystemBarPadding(activity, edgeToEdge.resolveStatusBarColor(activity))
            EdgeToEdgeMode.STATUS_BAR_ONLY -> EdgeToEdgeUtils.applyStatusBarBackground(activity, edgeToEdge.resolveStatusBarColor(activity))
            EdgeToEdgeMode.APP_INTRO -> EdgeToEdgeUtils.applyAppIntroInsets(activity, edgeToEdge.resolveStatusBarColor(activity))
            EdgeToEdgeMode.NONE -> Unit
        }
    }

    private fun EdgeToEdge?.resolveStatusBarColor(activity: Activity): Int {
        return ContextCompat.getColor(activity, this.statusBarColorOrDefault().colorResource())
    }

    private fun EdgeToEdge?.statusBarColorOrDefault(): EdgeToEdgeStatusBarColor {
        return this?.statusBarColor ?: EdgeToEdgeStatusBarColor.DEFAULT
    }

    @ColorRes
    private fun EdgeToEdgeStatusBarColor.colorResource(): Int {
        return when (this) {
            EdgeToEdgeStatusBarColor.DEFAULT -> R.color.colorPrimaryDark
            EdgeToEdgeStatusBarColor.CAMERA_INTRO -> R.color.md_teal_800
            EdgeToEdgeStatusBarColor.ASSIGNATION_INTRO -> R.color.md_green_800
        }
    }
}
