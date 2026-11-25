/package com.personal.movingwords

import android.content.Context

object WordStorage {
    private const val PREFS_NAME = "words_screensaver_prefs"
    private const val KEY_WORDS = "custom_words"

    private val DEFAULT_WORDS = listOf(
        "Jesus is the Son of God",
        "Ifa is a library of harmless ancient sacred knowledge.",
        "Money is important.",
        "Prayer actually works.",
        "The worst battle is in the mind.",
        "Na Today dey always settle the fight btw yesterday and tomorrow.",
        "Consider those who truly tried but still can't.",
        "Make Kindness your weapon",
        "Stop fearing what you do not understand. Instead learn about it.",
        "The concept of concave and convex actually makes so much sense"
    )

    fun saveWords(context: Context, rawText: String) {
        val cleaned = rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WORDS, cleaned.joinToString("\n")).apply()
    }

    fun loadWords(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_WORDS, null)
        if (saved.isNullOrBlank()) return DEFAULT_WORDS
        return saved.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
}