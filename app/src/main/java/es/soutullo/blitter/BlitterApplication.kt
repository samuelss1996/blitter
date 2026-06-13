package es.soutullo.blitter

import android.app.Activity
import android.app.Application
import android.os.Bundle
import es.soutullo.blitter.view.util.EdgeToEdgePolicy

class BlitterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                EdgeToEdgePolicy.prepare(activity)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                EdgeToEdgePolicy.prepare(activity)
                EdgeToEdgePolicy.apply(activity)
            }

            override fun onActivityStarted(activity: Activity) = Unit

            override fun onActivityResumed(activity: Activity) {
                EdgeToEdgePolicy.refresh(activity)
            }

            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
