package es.soutullo.blitter.model.billing

import android.content.Context
import androidx.preference.PreferenceManager
import es.soutullo.blitter.R

object AdsRemovalStore {
    private const val KEY_LAST_VALIDATION = "preference_ads_removed_last_validation"
    private const val VALIDATION_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L

    fun areAdsRemoved(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.preference_key_ads_removed), false)
    }

    fun markAdsRemoved(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.preference_key_ads_removed), true)
                .putLong(KEY_LAST_VALIDATION, System.currentTimeMillis())
                .apply()
    }

    fun markAdsNotRemoved(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.preference_key_ads_removed), false)
                .putLong(KEY_LAST_VALIDATION, System.currentTimeMillis())
                .apply()
    }

    fun isValidationStale(context: Context): Boolean {
        val lastValidation = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_LAST_VALIDATION, 0L)

        return System.currentTimeMillis() - lastValidation > VALIDATION_INTERVAL_MILLIS
    }

    fun markValidationChecked(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_LAST_VALIDATION, System.currentTimeMillis())
                .apply()
    }
}
