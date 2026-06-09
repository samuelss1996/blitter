package com.pavelsikun.seekbarpreference

import android.app.AlertDialog
import android.content.Context
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import es.soutullo.blitter.R

class SeekBarPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private val minValue: Int
    private val maxValue: Int
    private val interval: Int
    private val measurementUnit: String

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference)
        minValue = typedArray.getInt(R.styleable.SeekBarPreference_msbp_minValue, 0)
        maxValue = typedArray.getInt(R.styleable.SeekBarPreference_msbp_maxValue, 100)
        interval = typedArray.getInt(R.styleable.SeekBarPreference_msbp_interval, 1).coerceAtLeast(1)
        measurementUnit = typedArray.getString(R.styleable.SeekBarPreference_msbp_measurementUnit) ?: ""
        typedArray.recycle()
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        val fallback = (defaultValue as? Int) ?: 0
        persistInt(if (restorePersistedValue) getPersistedInt(fallback) else fallback)
    }

    override fun onClick() {
        val currentValue = getPersistedInt(minValue).coerceIn(minValue, maxValue)
        val label = TextView(context)
        val seekBar = SeekBar(context)
        val content = LinearLayout(context)

        label.textSize = 18f
        seekBar.max = ((maxValue - minValue) / interval).coerceAtLeast(0)
        seekBar.progress = ((currentValue - minValue) / interval).coerceAtLeast(0)
        updateLabel(label, currentValue)

        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(24), dp(8), dp(24), 0)
        content.addView(label)
        content.addView(seekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateLabel(label, minValue + progress * interval)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        AlertDialog.Builder(context)
                .setTitle(title)
                .setView(content)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    persistInt(minValue + seekBar.progress * interval)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    private fun updateLabel(label: TextView, value: Int) {
        label.text = "$value$measurementUnit"
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
