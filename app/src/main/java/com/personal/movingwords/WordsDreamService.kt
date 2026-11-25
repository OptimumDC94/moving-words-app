package com.personal.movingwords

import android.service.dreams.DreamService

class WordsDreamService : DreamService() {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = true

        val words = WordStorage.loadWords(this)
        setContentView(MovingWordsView(this, words, transparentBackground = false))
    }
}