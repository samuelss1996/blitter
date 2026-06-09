package es.soutullo.blitter

import android.app.Activity
import android.app.Application
import android.os.Bundle
import es.soutullo.blitter.view.activity.OcrCaptureActivity
import es.soutullo.blitter.view.util.EdgeToEdgeUtils

class BlitterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is OcrCaptureActivity) {
                    EdgeToEdgeUtils.applySystemBarPadding(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
