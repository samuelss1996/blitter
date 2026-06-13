package es.soutullo.blitter.view.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ColorProtection
import androidx.core.view.insets.ProtectionLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.appintro.R as AppIntroR
import es.soutullo.blitter.R
import io.github.kobakei.materialfabspeeddial.R as FabSpeedDialR

@Suppress("DEPRECATION")
object EdgeToEdgeUtils {
    private const val STATUS_BAR_BACKGROUND_TAG = "blitter_status_bar_background"
    private const val APP_INTRO_NAVIGATION_BAR_BACKGROUND_TAG = "blitter_app_intro_navigation_bar_background"

    fun prepareSystemBars(activity: Activity) {
        if (!activity.shouldHandleEdgeToEdge()) {
            return
        }

        activity.configureSystemBars()
    }

    fun applySystemBarPadding(activity: Activity, @ColorInt defaultStatusBarColor: Int = Color.TRANSPARENT) {
        if (!activity.shouldHandleEdgeToEdge()) {
            return
        }

        activity.applyNavigationBarStyle()
        setStatusBarColor(activity, defaultStatusBarColor)

        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val decor = activity.window.decorView as? FrameLayout ?: return

        decor.doOnApplyWindowInsets { windowInsets ->
            activity.applyNavigationBarStyle(requestInsets = false)
            content.applyInsetsToTaggedDescendants(activity, windowInsets.edgeToEdgeInsets())
            decor.setStatusBarBackground(
                height = windowInsets.statusBarInsetTop(),
                color = decor.statusBarColorOr(defaultStatusBarColor)
            )
        }
    }

    fun applyStatusBarBackground(activity: Activity, @ColorInt color: Int) {
        setStatusBarColor(activity, color)

        if (!activity.shouldHandleEdgeToEdge()) {
            return
        }

        activity.applyNavigationBarStyle()

        val decor = activity.window.decorView as? FrameLayout ?: return
        decor.doOnApplyWindowInsets { windowInsets ->
            activity.applyNavigationBarStyle(requestInsets = false)
            decor.setStatusBarBackground(
                height = windowInsets.statusBarInsetTop(),
                color = decor.statusBarColorOr(color)
            )
        }
    }

    fun applyAppIntroInsets(activity: Activity, @ColorInt statusBarColor: Int) {
        setStatusBarColor(activity, statusBarColor)

        if (!activity.shouldHandleEdgeToEdge()) {
            return
        }

        activity.applyNavigationBarStyle()

        val decor = activity.window.decorView as? FrameLayout ?: return
        decor.doOnApplyWindowInsets { windowInsets ->
            activity.applyNavigationBarStyle(requestInsets = false)
            val edgeToEdgeInsets = windowInsets.edgeToEdgeInsets()
            val color = decor.statusBarColorOr(statusBarColor)

            decor.setStatusBarBackground(windowInsets.statusBarInsetTop(), color)
            decor.setAppIntroNavigationBarBackground(windowInsets.gestureNavigationBarInsetBottom(), color)
            decor.moveAppIntroBottomControlsAboveNavigationBar(edgeToEdgeInsets.bottom)
        }
    }

    fun setStatusBarColorResource(activity: Activity, @ColorRes colorId: Int) {
        setStatusBarColor(activity, ContextCompat.getColor(activity, colorId))
    }

    fun refreshSystemBars(activity: Activity) {
        if (!activity.shouldHandleEdgeToEdge()) {
            return
        }

        activity.applyNavigationBarStyle()
        activity.window.decorView.requestApplyInsetsCompat()
        activity.findViewById<View>(android.R.id.content)?.requestApplyInsetsCompat()
        activity.findViewById<View>(R.id.edge_to_edge_navigation_bar_protection)?.requestApplyInsetsCompat()
    }

    fun setStatusBarColor(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        val decor = activity.window.decorView
        decor.setTag(R.id.edge_to_edge_status_bar_color, color)
        activity.window.statusBarColor = color

        if (Build.VERSION.SDK_INT >= 35) {
            (decor as? FrameLayout)?.updateStatusBarBackground(color)
        }
    }

    private fun Activity.shouldHandleEdgeToEdge(): Boolean {
        return Build.VERSION.SDK_INT >= 35
    }

    private fun Activity.configureSystemBars() {
        val decor = window.decorView
        val insetsController = WindowCompat.getInsetsController(window, decor)

        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = true

        val navigationBarColor = ContextCompat.getColor(this, R.color.md_white_1000)
        window.navigationBarColor = navigationBarColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = navigationBarColor
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun Activity.applyNavigationBarStyle(requestInsets: Boolean = true) {
        configureSystemBars()
        applyNavigationBarProtection(requestInsets = requestInsets)
    }

    private fun Activity.applyNavigationBarProtection(
        @ColorInt color: Int = ContextCompat.getColor(this, R.color.md_white_1000),
        requestInsets: Boolean = true
    ) {
        ensureNavigationBarProtectionLayout()
            ?.apply {
                if (getTag(R.id.edge_to_edge_navigation_bar_protection_color) != color) {
                    setTag(R.id.edge_to_edge_navigation_bar_protection_color, color)
                    setProtections(listOf(ColorProtection(WindowInsetsCompat.Side.BOTTOM, color)))
                }
                if (requestInsets) {
                    requestApplyInsetsCompat()
                }
            }
    }

    private fun Activity.ensureNavigationBarProtectionLayout(): ProtectionLayout? {
        val decor = window.decorView as? FrameLayout ?: return null
        decor.directNavigationBarProtectionLayout()?.let { return it }

        return ProtectionLayout(this).apply {
            id = R.id.edge_to_edge_navigation_bar_protection
            fitsSystemWindows = false
            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            decor.addView(
                this,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun FrameLayout.directNavigationBarProtectionLayout(): ProtectionLayout? {
        repeat(childCount) { index ->
            val child = getChildAt(index)
            if (child.id == R.id.edge_to_edge_navigation_bar_protection && child is ProtectionLayout) {
                return child
            }
        }
        return null
    }

    private fun FrameLayout.updateStatusBarBackground(@ColorInt color: Int) {
        findViewWithTag<View>(STATUS_BAR_BACKGROUND_TAG)?.setBackgroundColor(color)
    }

    private fun View.statusBarColorOr(@ColorInt defaultColor: Int): Int {
        return getTag(R.id.edge_to_edge_status_bar_color) as? Int ?: defaultColor
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

    private fun FrameLayout.setAppIntroNavigationBarBackground(height: Int, @ColorInt color: Int) {
        val background = findViewWithTag<View>(APP_INTRO_NAVIGATION_BAR_BACKGROUND_TAG)
            ?: View(context).also { view ->
                view.tag = APP_INTRO_NAVIGATION_BAR_BACKGROUND_TAG
                view.isClickable = false
                view.isFocusable = false
                view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                addView(
                    view,
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, Gravity.BOTTOM)
                )
            }

        background.layoutParams = (background.layoutParams as FrameLayout.LayoutParams).apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            this.height = height.coerceAtLeast(0)
            gravity = Gravity.BOTTOM
        }
        background.visibility = if (height > 0) View.VISIBLE else View.GONE
        background.setBackgroundColor(color)
        background.bringToFront()
    }

    private fun ViewGroup.moveAppIntroBottomControlsAboveNavigationBar(bottomInset: Int) {
        listOf(
            AppIntroR.id.bottom,
            AppIntroR.id.skip,
            AppIntroR.id.back,
            AppIntroR.id.indicator_container,
            AppIntroR.id.next,
            AppIntroR.id.done
        ).forEach { viewId ->
            findViewById<View>(viewId)?.setBottomMargin(bottomInset)
        }
    }

    private fun View.doOnApplyWindowInsets(callback: (WindowInsetsCompat) -> Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            callback(insets)
            insets
        }
        requestApplyInsetsCompat()
    }

    private fun View.requestApplyInsetsCompat() {
        ViewCompat.requestApplyInsets(this)
    }

    private fun WindowInsetsCompat.statusBarInsetTop(): Int {
        return getInsets(WindowInsetsCompat.Type.statusBars()).top
    }

    private fun WindowInsetsCompat.edgeToEdgeInsets(): EdgeToEdgeInsets {
        val systemBars = getInsets(WindowInsetsCompat.Type.systemBars())
        val statusBars = getInsets(WindowInsetsCompat.Type.statusBars())
        val navigationBars = getInsets(WindowInsetsCompat.Type.navigationBars())
        val tappableElement = getInsets(WindowInsetsCompat.Type.tappableElement())

        return EdgeToEdgeInsets(
            left = systemBars.left,
            statusTop = statusBars.top,
            right = systemBars.right,
            bottom = maxOf(navigationBars.bottom, tappableElement.bottom)
        )
    }

    private fun WindowInsetsCompat.gestureNavigationBarInsetBottom(): Int {
        val navigationBars = getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        val tappableElement = getInsets(WindowInsetsCompat.Type.tappableElement()).bottom
        return (navigationBars - minOf(navigationBars, tappableElement)).coerceAtLeast(0)
    }

    private fun ViewGroup.applyInsetsToTaggedDescendants(activity: Activity, insets: EdgeToEdgeInsets) {
        this.forEachDescendant { view ->
            val tags = view.edgeToEdgeTags()

            when {
                tags.has(activity, R.string.edge_to_edge_app_bar_tag) -> {
                    view.applySystemBarPadding(insets, top = true)
                }
                tags.has(activity, R.string.edge_to_edge_fab_aware_scrolling_content_tag) -> {
                    view.applyScrollingContentInsets(
                        insets = insets,
                        extraBottomPadding = view.resources.getDimensionPixelSize(R.dimen.fab_content_bottom_padding)
                    )
                }
                tags.has(activity, R.string.edge_to_edge_scrolling_content_tag) -> {
                    view.applyScrollingContentInsets(insets)
                }
                tags.has(activity, R.string.edge_to_edge_bottom_controls_tag) -> {
                    view.applySystemBarMargins(insets, bottom = true)
                }
                tags.has(activity, R.string.edge_to_edge_bottom_padded_content_tag) -> {
                    view.applySystemBarPadding(insets, bottom = true)
                }
                tags.has(activity, R.string.edge_to_edge_fab_speed_dial_tag) -> {
                    view.applyFabSpeedDialInsets(insets)
                }
            }

            if (tags.has(activity, R.string.edge_to_edge_action_bar_content_tag)) {
                view.applyActionBarContentInsets(activity, insets)
            }
        }
    }

    private fun ViewGroup.forEachDescendant(action: (View) -> Unit) {
        repeat(childCount) { index ->
            val child = getChildAt(index)
            action(child)
            (child as? ViewGroup)?.forEachDescendant(action)
        }
    }

    private fun View.edgeToEdgeTags(): Set<String> {
        return tag
            ?.toString()
            ?.split(' ')
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()
    }

    private fun Set<String>.has(activity: Activity, @StringRes tagRes: Int): Boolean {
        return contains(activity.getString(tagRes))
    }

    private fun View.applySystemBarMargins(
        insets: EdgeToEdgeInsets,
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val initialMargins = initialMargins()

        setMargins(
            initialMargins.copy(
                left = initialMargins.left + if (left) insets.left else 0,
                top = initialMargins.top + if (top) insets.statusTop else 0,
                right = initialMargins.right + if (right) insets.right else 0,
                bottom = initialMargins.bottom + if (bottom) insets.bottom else 0
            )
        )
    }

    private fun View.applyScrollingContentInsets(insets: EdgeToEdgeInsets, extraBottomPadding: Int = 0) {
        (this as? RecyclerView)?.clipToPadding = false
        this.applySystemBarPadding(
            insets = insets,
            left = true,
            right = true,
            bottom = true,
            extraBottomPadding = extraBottomPadding
        )
    }

    private fun View.applyFabSpeedDialInsets(insets: EdgeToEdgeInsets) {
        val fabsContainer = findViewById<View>(FabSpeedDialR.id.fabs_container) ?: return
        val margins = fabsContainer.initialMargins()

        fabsContainer.setMargins(
            margins.copy(
                right = margins.right + insets.right,
                bottom = margins.bottom + insets.bottom
            )
        )
    }

    private fun View.applyActionBarContentInsets(activity: Activity, insets: EdgeToEdgeInsets) {
        val initialMargins = initialMargins()

        setMargins(
            initialMargins.copy(
                left = initialMargins.left + insets.left,
                top = initialMargins.top + activity.actionBarContentTop(insets),
                right = initialMargins.right + insets.right
            )
        )
    }

    private fun Activity.actionBarContentTop(insets: EdgeToEdgeInsets): Int {
        val actionBar = findViewById<View>(androidx.appcompat.R.id.action_bar_container) ?: return 0
        val measuredBottom = actionBar.bottom.takeIf { it > 0 } ?: (actionBar.top + actionBar.measuredHeight).takeIf { it > 0 }
        val themedBottom = insets.statusTop + actionBarSize()

        return maxOf(measuredBottom ?: 0, themedBottom)
    }

    private fun Activity.actionBarSize(): Int {
        val typedValue = TypedValue()
        return if (theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            0
        }
    }

    private fun View.applySystemBarPadding(
        insets: EdgeToEdgeInsets,
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false,
        extraBottomPadding: Int = 0
    ) {
        val initialPadding = initialPadding()

        setPadding(
            initialPadding.copy(
                left = initialPadding.left + if (left) insets.left else 0,
                top = initialPadding.top + if (top) insets.statusTop else 0,
                right = initialPadding.right + if (right) insets.right else 0,
                bottom = initialPadding.bottom + if (bottom) insets.bottom + extraBottomPadding else 0
            )
        )
    }

    private fun View.initialMargins(): MarginSnapshot {
        return getTag(R.id.edge_to_edge_initial_margins) as? MarginSnapshot
            ?: marginSnapshot().also { setTag(R.id.edge_to_edge_initial_margins, it) }
    }

    private fun View.initialPadding(): PaddingSnapshot {
        return getTag(R.id.edge_to_edge_initial_padding) as? PaddingSnapshot
            ?: paddingSnapshot().also { setTag(R.id.edge_to_edge_initial_padding, it) }
    }

    private fun View.marginSnapshot(): MarginSnapshot {
        val margins = layoutParams as? ViewGroup.MarginLayoutParams
        return MarginSnapshot(
            left = margins?.leftMargin ?: 0,
            top = margins?.topMargin ?: 0,
            right = margins?.rightMargin ?: 0,
            bottom = margins?.bottomMargin ?: 0
        )
    }

    private fun View.setMargins(margins: MarginSnapshot) {
        val marginLayoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
        marginLayoutParams.setMargins(margins.left, margins.top, margins.right, margins.bottom)
        layoutParams = marginLayoutParams
    }

    private fun View.setBottomMargin(bottomMargin: Int) {
        val initialMargins = initialMargins()
        setMargins(initialMargins.copy(bottom = initialMargins.bottom + bottomMargin))
    }

    private fun View.paddingSnapshot(): PaddingSnapshot {
        return PaddingSnapshot(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    private fun View.setPadding(padding: PaddingSnapshot) {
        setPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    private data class MarginSnapshot(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )

    private data class PaddingSnapshot(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )

    private data class EdgeToEdgeInsets(
        val left: Int,
        val statusTop: Int,
        val right: Int,
        val bottom: Int
    )
}
