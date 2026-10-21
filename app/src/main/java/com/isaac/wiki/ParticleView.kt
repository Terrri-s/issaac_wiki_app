package com.isaac.wiki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

/**
 * 首页漂浮粒子层：拾取物小图标缓缓飘落、轻微摇摆，营造游戏氛围。
 * 精灵图取自 assets/img 的真实游戏图标，缺图时自动跳过。
 */
class ParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val sprites: List<Bitmap> = listOf(
        "icon_Heart.png", "icon_Coin.png", "icon_Key.png", "icon_Bomb.png",
        "icon_SoulHeart.png", "icon_Battery.png", "icon_Card.png", "icon_Pill.png",
        "icon_Nickel.png", "icon_Dime.png"
    ).mapNotNull { n ->
        try {
            context.assets.open("img/$n").use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }
    }

    private class Particle(
        var x: Float, var y: Float,
        val speed: Float, val sway: Float, val phase: Float,
        val size: Float, val alpha: Int, val bmp: Bitmap
    )

    private val particles = mutableListOf<Particle>()
    private val rnd = Random(11)
    private var lastTime = 0L

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        if (w == 0 || sprites.isEmpty()) return
        particles.clear()
        val density = resources.displayMetrics.density
        repeat(14) {
            val bmp = sprites[rnd.nextInt(sprites.size)]
            val size = (16f + rnd.nextInt(18)) * density
            particles.add(
                Particle(
                    x = rnd.nextInt(w).toFloat(),
                    y = rnd.nextInt(h).toFloat(),
                    speed = (24f + rnd.nextInt(40)) * density,
                    sway = (10f + rnd.nextInt(24)) * density,
                    phase = rnd.nextFloat() * 6.28f,
                    size = size,
                    alpha = 40 + rnd.nextInt(45),
                    bmp = bmp
                )
            )
        }
        lastTime = SystemClock.uptimeMillis()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (particles.isEmpty()) return
        val now = SystemClock.uptimeMillis()
        val dt = ((now - lastTime).coerceIn(0, 50)) / 1000f
        lastTime = now
        val h = height.toFloat()
        for (p in particles) {
            p.y += p.speed * dt
            if (p.y - p.size > h) {
                p.y = -p.size
            }
            val dx = sin((now / 900f) + p.phase) * p.sway
            paint.alpha = p.alpha
            canvas.drawBitmap(p.bmp, null, android.graphics.RectF(
                p.x + dx - p.size / 2, p.y - p.size / 2,
                p.x + dx + p.size / 2, p.y + p.size / 2
            ), paint)
        }
        postInvalidateOnAnimation()
    }
}
