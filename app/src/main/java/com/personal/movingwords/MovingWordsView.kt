package com.personal.movingwords

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class MovingWordsView(
    context: Context,
    private val wordList: List<String>,
    private val transparentBackground: Boolean = false
) : View(context) {

    private data class WordSprite(
        val layout: StaticLayout,
        val width: Float,
        val height: Float,
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float
    )

    private val palette = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#FFE66D"),
        Color.parseColor("#A29BFE"),
        Color.parseColor("#FD79A8"),
        Color.parseColor("#81ECEC"),
        Color.WHITE
    )

    private val sprites = mutableListOf<WordSprite>()
    private var lastFrameNanos = 0L
    private var isRunning = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRunning = true
        lastFrameNanos = 0L
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        isRunning = false
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && sprites.isEmpty()) {
            initSprites(w.toFloat(), h.toFloat())
        }
    }

    private fun initSprites(screenW: Float, screenH: Float) {
        val scaledDensity = resources.displayMetrics.scaledDensity
        val density = resources.displayMetrics.density
        val maxBoxWidth = (screenW * 0.68f).toInt().coerceAtLeast(100)
        sprites.clear()

        for (phrase in wordList) {
            val spSize = Random.nextInt(14, 21).toFloat()
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.random()
                textSize = spSize * scaledDensity
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                if (transparentBackground) {
                    setShadowLayer(6f, 2f, 2f, Color.argb(200, 0, 0, 0))
                }
            }

            val layout = StaticLayout.Builder
                .obtain(phrase, 0, phrase.length, textPaint, maxBoxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(false)
                .build()

            var actualWidth = 0f
            for (i in 0 until layout.lineCount) {
                actualWidth = max(actualWidth, layout.getLineWidth(i))
            }
            val actualHeight = layout.height.toFloat()

            val maxX = max(1f, screenW - actualWidth)
            val maxY = max(1f, screenH - actualHeight)

            // Calm, readable speed (35dp to 80dp per second)
            val vx = (if (Random.nextBoolean()) 1 else -1) * Random.nextInt(35, 80) * density
            val vy = (if (Random.nextBoolean()) 1 else -1) * Random.nextInt(35, 80) * density

            sprites.add(
                WordSprite(
                    layout = layout,
                    width = actualWidth,
                    height = actualHeight,
                    x = Random.nextFloat() * maxX,
                    y = Random.nextFloat() * maxY,
                    vx = vx,
                    vy = vy
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!transparentBackground) {
            canvas.drawColor(Color.BLACK)
        }

        val now = System.nanoTime()
        val dt = if (lastFrameNanos == 0L) 0f else ((now - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.05f)
        lastFrameNanos = now

        val screenW = width.toFloat()
        val screenH = height.toFloat()

        for (s in sprites) {
            s.x += s.vx * dt
            s.y += s.vy * dt

            if (s.x <= 0f) {
                s.x = 0f
                s.vx = abs(s.vx)
            } else if (s.x + s.width >= screenW) {
                s.x = max(0f, screenW - s.width)
                s.vx = -abs(s.vx)
            }

            if (s.y <= 0f) {
                s.y = 0f
                s.vy = abs(s.vy)
            } else if (s.y + s.height >= screenH) {
                s.y = max(0f, screenH - s.height)
                s.vy = -abs(s.vy)
            }

            canvas.save()
            canvas.translate(s.x, s.y)
            s.layout.draw(canvas)
            canvas.restore()
        }

        if (isRunning) {
            postInvalidateOnAnimation()
        }
    }
}