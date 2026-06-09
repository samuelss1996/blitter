package com.amulyakhare.textdrawable.util

class ColorGenerator private constructor(private val colors: List<Int>) {
    fun getColor(key: Any?): Int {
        val hash = key?.hashCode() ?: 0
        val index = Math.abs(hash) % colors.size

        return colors[index]
    }

    companion object {
        @JvmField
        val MATERIAL = ColorGenerator(listOf(
                0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(),
                0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF009688.toInt(),
                0xFF4CAF50.toInt(), 0xFFFF9800.toInt(), 0xFF795548.toInt(),
                0xFF607D8B.toInt()
        ))
    }
}
