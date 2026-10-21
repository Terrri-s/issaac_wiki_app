package com.isaac.wiki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.LruCache

/**
 * 运行时图标生成器：根据条目名称关键词绘制对应图案（心/钥匙/炸弹/卡牌/骷髅…），
 * 每个词条都拥有独立配色与图案的图片，无需打包图片资源。
 */
object ItemIconGenerator {

    private val cache = LruCache<String, Bitmap>(800)

    fun icon(context: Context, name: String, tags: String): Bitmap {
        val key = "$tags|$name"
        cache.get(key)?.let { return it }
        val density = context.resources.displayMetrics.density
        val size = (72 * density).toInt()
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 底板：深色圆角 + 类型描边
        val accent = colorFor(tags, name)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(34, 20, 26) }
        val s = size.toFloat()
        canvas.drawRoundRect(RectF(0f, 0f, s, s), s * 0.2f, s * 0.2f, bg)
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = s * 0.035f
            color = accent
        }
        canvas.drawRoundRect(RectF(s * 0.07f, s * 0.07f, s * 0.93f, s * 0.93f), s * 0.16f, s * 0.16f, ring)

        // 图案
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
        val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(60, 255, 255, 255) }
        drawGlyph(canvas, shapeFor(name, tags), s, fill, shade, accent)

        cache.put(key, bmp)
        return bmp
    }

    /** 名称关键词 -> 图案 */
    private fun shapeFor(name: String, tags: String): String {
        val n = name + tags
        return when {
            n.contains("红心") || n.contains("心脏") || n.contains("魂心") ||
                n.contains("黑心") || n.contains("骨心") || n.contains("金心") ||
                n.contains("碎心") || n.contains("永恒心") || n.contains("心之") -> "heart"
            n.contains("钥匙") || n.contains("钥匙链") -> "key"
            n.contains("炸弹") -> "bomb"
            n.contains("胶囊") || n.contains("药") || n.contains("丸") -> "pill"
            n.contains("卡牌") || n.contains("塔罗") || n.contains("卡 /") || n.contains("符文") -> "card"
            n.contains("星") || n.contains("座") -> "star"
            n.contains("骷髅") || n.contains("死亡") || n.contains("死猫") || n.contains("死鸟") -> "skull"
            n.contains("书") || n.contains("魔典") -> "book"
            n.contains("刀") || n.contains("剃") || n.contains("刃") || n.contains("剑") -> "blade"
            n.contains("苍蝇") || n.contains("蜘蛛") || n.contains("虫") || n.contains("蜂") -> "bug"
            n.contains("宝宝") || n.contains("婴儿") || n.contains("宝贝") || n.contains("孩") -> "baby"
            n.contains("蘑菇") -> "mushroom"
            n.contains("火") || n.contains("焰") || n.contains("蜡烛") || n.contains("燃烧") -> "flame"
            n.contains("翅膀") || n.contains("羽") || n.contains("飞行") || n.contains("天使") -> "wing"
            n.contains("角") -> "horn"
            n.contains("电") || n.contains("雷") -> "bolt"
            n.contains("冰") || n.contains("雪") -> "snow"
            n.contains("毒") || n.contains("酸") -> "drop"
            n.contains("地图") || n.contains("罗盘") || n.contains("心灵") -> "map"
            n.contains("盾") || n.contains("披风") || n.contains("圣光") || n.contains("保护") -> "shield"
            n.contains("王冠") || n.contains("王") -> "crown"
            n.contains("骰") || n.contains("D6") || n.contains("D4") || n.contains("D20") || n.contains("D100") -> "dice"
            n.contains("月") -> "moon"
            n.contains("太阳") || n.contains("日") -> "sun"
            n.contains("时") || n.contains("钟") || n.contains("沙漏") -> "clock"
            n.contains("手") || n.contains("爪") || n.contains("指") -> "hand"
            n.contains("脚") || n.contains("蹄") || n.contains("马") -> "hoof"
            n.contains("奶") || n.contains("瓶") || n.contains("水") -> "bottle"
            n.contains("弹") || n.contains("导弹") || n.contains("飞弹") -> "missile"
            n.contains("恶魔") || n.contains("鬼") || n.contains("撒旦") || n.contains("魔物") -> "devil"
            n.contains("梯") -> "ladder"
            n.contains("花") -> "flower"
            n.contains("硬币") || n.contains("钱") || n.contains("便士") || n.contains("镍") || n.contains("宝箱") || n.contains("箱") -> "coin"
            n.contains("泪") || n.contains("眼") -> "tear"
            n.contains("头") -> "head"
            n.contains("骨") -> "bone"
            n.contains("诅咒") || n.contains("诅") -> "curse"
            else -> "tear"
        }
    }

    /** 绘制各图案 */
    private fun drawGlyph(canvas: Canvas, shape: String, s: Float, fill: Paint, shade: Paint, accent: Int) {
        val cx = s * 0.5f
        val cy = s * 0.5f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = s * 0.06f
            color = fill.color
            strokeCap = Paint.Cap.ROUND
        }
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        when (shape) {
            "heart" -> {
                val p = Path().apply {
                    moveTo(cx, cy + s * 0.22f)
                    cubicTo(cx - s * 0.34f, cy + s * 0.02f, cx - s * 0.2f, cy - s * 0.26f, cx, cy - s * 0.08f)
                    cubicTo(cx + s * 0.2f, cy - s * 0.26f, cx + s * 0.34f, cy + s * 0.02f, cx, cy + s * 0.22f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "key" -> {
                canvas.drawCircle(cx - s * 0.16f, cy - s * 0.12f, s * 0.12f, line)
                val p = Path().apply {
                    moveTo(cx - s * 0.08f, cy - s * 0.04f)
                    lineTo(cx + s * 0.24f, cy + s * 0.24f)
                    moveTo(cx + s * 0.12f, cy + s * 0.12f)
                    lineTo(cx + s * 0.2f, cy + s * 0.04f)
                }
                canvas.drawPath(p, line)
            }
            "bomb" -> {
                canvas.drawCircle(cx, cy + s * 0.05f, s * 0.2f, fill)
                val p = Path().apply {
                    moveTo(cx + s * 0.08f, cy - s * 0.16f)
                    lineTo(cx + s * 0.2f, cy - s * 0.28f)
                }
                canvas.drawPath(p, line)
                canvas.drawCircle(cx + s * 0.22f, cy - s * 0.3f, s * 0.05f, fill)
            }
            "pill" -> {
                val r = RectF(cx - s * 0.26f, cy - s * 0.12f, cx + s * 0.26f, cy + s * 0.12f)
                canvas.drawRoundRect(r, s * 0.12f, s * 0.12f, fill)
                val sep = Path().apply {
                    moveTo(cx, cy - s * 0.12f)
                    lineTo(cx, cy + s * 0.12f)
                }
                canvas.drawPath(sep, line)
            }
            "card" -> {
                val r = RectF(cx - s * 0.18f, cy - s * 0.26f, cx + s * 0.18f, cy + s * 0.26f)
                canvas.drawRoundRect(r, s * 0.05f, s * 0.05f, fill)
                canvas.drawCircle(cx, cy, s * 0.08f, white)
            }
            "star" -> {
                val p = Path()
                val outer = s * 0.26f
                val inner = s * 0.12f
                for (i in 0 until 10) {
                    val rad = if (i % 2 == 0) outer else inner
                    val ang = Math.PI / 5 * i - Math.PI / 2
                    val x = cx + (rad * Math.cos(ang)).toFloat()
                    val y = cy + (rad * Math.sin(ang)).toFloat()
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                p.close()
                canvas.drawPath(p, fill)
            }
            "skull" -> {
                canvas.drawCircle(cx, cy - s * 0.06f, s * 0.2f, fill)
                val jaw = RectF(cx - s * 0.12f, cy + s * 0.1f, cx + s * 0.12f, cy + s * 0.24f)
                canvas.drawRoundRect(jaw, s * 0.04f, s * 0.04f, fill)
                canvas.drawCircle(cx - s * 0.08f, cy - s * 0.08f, s * 0.05f, shade)
                canvas.drawCircle(cx + s * 0.08f, cy - s * 0.08f, s * 0.05f, shade)
            }
            "book" -> {
                val r = RectF(cx - s * 0.22f, cy - s * 0.24f, cx + s * 0.22f, cy + s * 0.24f)
                canvas.drawRoundRect(r, s * 0.04f, s * 0.04f, fill)
                val spine = Path().apply {
                    moveTo(cx, cy - s * 0.24f)
                    lineTo(cx, cy + s * 0.24f)
                }
                canvas.drawPath(spine, line)
            }
            "blade" -> {
                val p = Path().apply {
                    moveTo(cx - s * 0.22f, cy + s * 0.22f)
                    lineTo(cx + s * 0.18f, cy - s * 0.2f)
                    lineTo(cx + s * 0.24f, cy - s * 0.12f)
                    lineTo(cx - s * 0.14f, cy + s * 0.26f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "bug" -> {
                canvas.drawOval(RectF(cx - s * 0.14f, cy - s * 0.18f, cx + s * 0.14f, cy + s * 0.22f), fill)
                canvas.drawCircle(cx, cy - s * 0.22f, s * 0.08f, fill)
                for (i in -1..1) {
                    val yy = cy + i * s * 0.12f
                    canvas.drawLine(cx - s * 0.14f, yy, cx - s * 0.26f, yy - s * 0.06f, line)
                    canvas.drawLine(cx + s * 0.14f, yy, cx + s * 0.26f, yy - s * 0.06f, line)
                }
            }
            "baby" -> {
                canvas.drawCircle(cx, cy, s * 0.22f, fill)
                canvas.drawCircle(cx - s * 0.08f, cy - s * 0.05f, s * 0.04f, shade)
                canvas.drawCircle(cx + s * 0.08f, cy - s * 0.05f, s * 0.04f, shade)
                val mouth = Path().apply {
                    moveTo(cx - s * 0.08f, cy + s * 0.1f)
                    lineTo(cx + s * 0.08f, cy + s * 0.1f)
                }
                canvas.drawPath(mouth, line)
            }
            "mushroom" -> {
                val cap = Path().apply {
                    addArc(RectF(cx - s * 0.24f, cy - s * 0.24f, cx + s * 0.24f, cy + s * 0.1f), 180f, 180f)
                }
                canvas.drawPath(cap, fill)
                val stem = RectF(cx - s * 0.1f, cy - s * 0.04f, cx + s * 0.1f, cy + s * 0.24f)
                canvas.drawRoundRect(stem, s * 0.05f, s * 0.05f, fill)
            }
            "flame" -> {
                val p = Path().apply {
                    moveTo(cx, cy - s * 0.28f)
                    cubicTo(cx + s * 0.22f, cy - s * 0.1f, cx + s * 0.18f, cy + s * 0.12f, cx, cy + s * 0.24f)
                    cubicTo(cx - s * 0.18f, cy + s * 0.12f, cx - s * 0.22f, cy - s * 0.1f, cx, cy - s * 0.28f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "wing" -> {
                val p = Path().apply {
                    moveTo(cx - s * 0.04f, cy + s * 0.18f)
                    cubicTo(cx - s * 0.3f, cy + s * 0.1f, cx - s * 0.3f, cy - s * 0.2f, cx - s * 0.04f, cy - s * 0.12f)
                    close()
                }
                canvas.drawPath(p, fill)
                val p2 = Path().apply {
                    moveTo(cx + s * 0.04f, cy + s * 0.18f)
                    cubicTo(cx + s * 0.3f, cy + s * 0.1f, cx + s * 0.3f, cy - s * 0.2f, cx + s * 0.04f, cy - s * 0.12f)
                    close()
                }
                canvas.drawPath(p2, fill)
            }
            "horn" -> {
                val p = Path().apply {
                    moveTo(cx - s * 0.16f, cy + s * 0.22f)
                    cubicTo(cx - s * 0.1f, cy - s * 0.1f, cx + s * 0.16f, cy - s * 0.24f, cx + s * 0.22f, cy - s * 0.28f)
                    cubicTo(cx + s * 0.16f, cy - s * 0.06f, cx + s * 0.1f, cy + s * 0.12f, cx + s * 0.16f, cy + s * 0.22f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "bolt" -> {
                val p = Path().apply {
                    moveTo(cx + s * 0.1f, cy - s * 0.28f)
                    lineTo(cx - s * 0.14f, cy + s * 0.02f)
                    lineTo(cx - s * 0.02f, cy + s * 0.02f)
                    lineTo(cx - s * 0.1f, cy + s * 0.28f)
                    lineTo(cx + s * 0.14f, cy - s * 0.02f)
                    lineTo(cx + s * 0.02f, cy - s * 0.02f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "snow" -> {
                for (i in 0 until 3) {
                    val ang = Math.PI / 3 * i
                    val dx = (s * 0.24 * Math.cos(ang)).toFloat()
                    val dy = (s * 0.24 * Math.sin(ang)).toFloat()
                    canvas.drawLine(cx - dx, cy - dy, cx + dx, cy + dy, line)
                }
            }
            "drop" -> {
                val p = Path().apply {
                    moveTo(cx, cy - s * 0.26f)
                    cubicTo(cx + s * 0.18f, cy - s * 0.06f, cx + s * 0.18f, cy + s * 0.1f, cx, cy + s * 0.2f)
                    cubicTo(cx - s * 0.18f, cy + s * 0.1f, cx - s * 0.18f, cy - s * 0.06f, cx, cy - s * 0.26f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "map" -> {
                val r = RectF(cx - s * 0.24f, cy - s * 0.2f, cx + s * 0.24f, cy + s * 0.2f)
                canvas.drawRoundRect(r, s * 0.05f, s * 0.05f, fill)
                val line1 = Path().apply {
                    moveTo(cx - s * 0.12f, cy - s * 0.08f)
                    lineTo(cx + s * 0.12f, cy - s * 0.08f)
                    moveTo(cx - s * 0.12f, cy + s * 0.04f)
                    lineTo(cx + s * 0.06f, cy + s * 0.04f)
                }
                canvas.drawPath(line1, line)
            }
            "shield" -> {
                val p = Path().apply {
                    moveTo(cx, cy - s * 0.24f)
                    lineTo(cx + s * 0.2f, cy - s * 0.14f)
                    lineTo(cx + s * 0.2f, cy + s * 0.06f)
                    lineTo(cx, cy + s * 0.26f)
                    lineTo(cx - s * 0.2f, cy + s * 0.06f)
                    lineTo(cx - s * 0.2f, cy - s * 0.14f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "crown" -> {
                val p = Path().apply {
                    moveTo(cx - s * 0.24f, cy + s * 0.18f)
                    lineTo(cx - s * 0.24f, cy - s * 0.12f)
                    lineTo(cx - s * 0.1f, cy + s * 0.02f)
                    lineTo(cx, cy - s * 0.2f)
                    lineTo(cx + s * 0.1f, cy + s * 0.02f)
                    lineTo(cx + s * 0.24f, cy - s * 0.12f)
                    lineTo(cx + s * 0.24f, cy + s * 0.18f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "dice" -> {
                val r = RectF(cx - s * 0.22f, cy - s * 0.22f, cx + s * 0.22f, cy + s * 0.22f)
                canvas.drawRoundRect(r, s * 0.06f, s * 0.06f, fill)
                canvas.drawCircle(cx - s * 0.1f, cy - s * 0.1f, s * 0.04f, shade)
                canvas.drawCircle(cx + s * 0.1f, cy - s * 0.1f, s * 0.04f, shade)
                canvas.drawCircle(cx, cy, s * 0.04f, shade)
                canvas.drawCircle(cx - s * 0.1f, cy + s * 0.1f, s * 0.04f, shade)
                canvas.drawCircle(cx + s * 0.1f, cy + s * 0.1f, s * 0.04f, shade)
            }
            "moon" -> {
                val outer = Path().apply {
                    addArc(RectF(cx - s * 0.24f, cy - s * 0.24f, cx + s * 0.24f, cy + s * 0.24f), 90f, 180f)
                }
                canvas.drawPath(outer, fill)
                val inner = Path().apply {
                    addArc(RectF(cx - s * 0.1f, cy - s * 0.24f, cx + s * 0.38f, cy + s * 0.24f), 90f, 180f)
                }
                canvas.drawPath(inner, shade)
            }
            "sun" -> {
                canvas.drawCircle(cx, cy, s * 0.14f, fill)
                for (i in 0 until 8) {
                    val ang = Math.PI / 4 * i
                    val x1 = cx + (s * 0.2 * Math.cos(ang)).toFloat()
                    val y1 = cy + (s * 0.2 * Math.sin(ang)).toFloat()
                    val x2 = cx + (s * 0.28 * Math.cos(ang)).toFloat()
                    val y2 = cy + (s * 0.28 * Math.sin(ang)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, line)
                }
            }
            "clock" -> {
                canvas.drawCircle(cx, cy, s * 0.22f, line)
                canvas.drawLine(cx, cy, cx, cy - s * 0.14f, line)
                canvas.drawLine(cx, cy, cx + s * 0.1f, cy, line)
            }
            "hand" -> {
                val r = RectF(cx - s * 0.12f, cy - s * 0.1f, cx + s * 0.12f, cy + s * 0.24f)
                canvas.drawRoundRect(r, s * 0.06f, s * 0.06f, fill)
                for (i in 0 until 4) {
                    val x = cx - s * 0.12f + i * s * 0.08f
                    canvas.drawLine(x + s * 0.02f, cy - s * 0.1f, x + s * 0.02f, cy - s * 0.26f, line)
                }
            }
            "hoof" -> {
                val p = Path().apply {
                    moveTo(cx - s * 0.18f, cy + s * 0.22f)
                    lineTo(cx - s * 0.1f, cy - s * 0.18f)
                    lineTo(cx + s * 0.1f, cy - s * 0.18f)
                    lineTo(cx + s * 0.18f, cy + s * 0.22f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
            "bottle" -> {
                val r = RectF(cx - s * 0.16f, cy - s * 0.14f, cx + s * 0.16f, cy + s * 0.26f)
                canvas.drawRoundRect(r, s * 0.08f, s * 0.08f, fill)
                canvas.drawRect(RectF(cx - s * 0.07f, cy - s * 0.28f, cx + s * 0.07f, cy - s * 0.12f), fill)
            }
            "missile" -> {
                val p = Path().apply {
                    moveTo(cx, cy - s * 0.28f)
                    lineTo(cx + s * 0.12f, cy + s * 0.16f)
                    lineTo(cx, cy + s * 0.08f)
                    lineTo(cx - s * 0.12f, cy + s * 0.16f)
                    close()
                }
                canvas.drawPath(p, fill)
                canvas.drawCircle(cx, cy - s * 0.12f, s * 0.05f, shade)
            }
            "devil" -> {
                canvas.drawCircle(cx, cy + s * 0.04f, s * 0.16f, fill)
                val p = Path().apply {
                    moveTo(cx - s * 0.16f, cy - s * 0.06f)
                    lineTo(cx - s * 0.26f, cy - s * 0.26f)
                    lineTo(cx - s * 0.06f, cy - s * 0.14f)
                    close()
                }
                canvas.drawPath(p, fill)
                val p2 = Path().apply {
                    moveTo(cx + s * 0.16f, cy - s * 0.06f)
                    lineTo(cx + s * 0.26f, cy - s * 0.26f)
                    lineTo(cx + s * 0.06f, cy - s * 0.14f)
                    close()
                }
                canvas.drawPath(p2, fill)
                canvas.drawCircle(cx - s * 0.06f, cy + s * 0.02f, s * 0.03f, shade)
                canvas.drawCircle(cx + s * 0.06f, cy + s * 0.02f, s * 0.03f, shade)
            }
            "ladder" -> {
                canvas.drawLine(cx - s * 0.12f, cy - s * 0.26f, cx - s * 0.12f, cy + s * 0.26f, line)
                canvas.drawLine(cx + s * 0.12f, cy - s * 0.26f, cx + s * 0.12f, cy + s * 0.26f, line)
                for (i in -1..1) {
                    val y = cy + i * s * 0.14f
                    canvas.drawLine(cx - s * 0.12f, y, cx + s * 0.12f, y, line)
                }
            }
            "flower" -> {
                for (i in 0 until 6) {
                    val ang = Math.PI / 3 * i
                    val x = cx + (s * 0.16 * Math.cos(ang)).toFloat()
                    val y = cy + (s * 0.16 * Math.sin(ang)).toFloat()
                    canvas.drawCircle(x, y, s * 0.08f, fill)
                }
                canvas.drawCircle(cx, cy, s * 0.08f, shade)
            }
            "coin" -> {
                canvas.drawCircle(cx, cy, s * 0.22f, fill)
                canvas.drawCircle(cx, cy, s * 0.15f, line)
            }
            "head" -> {
                canvas.drawCircle(cx, cy, s * 0.22f, fill)
                canvas.drawCircle(cx - s * 0.08f, cy - s * 0.04f, s * 0.045f, shade)
                canvas.drawCircle(cx + s * 0.08f, cy - s * 0.04f, s * 0.045f, shade)
            }
            "bone" -> {
                canvas.drawLine(cx - s * 0.18f, cy + s * 0.18f, cx + s * 0.18f, cy - s * 0.18f, line)
                canvas.drawCircle(cx - s * 0.2f, cy + s * 0.2f, s * 0.06f, fill)
                canvas.drawCircle(cx - s * 0.12f, cy + s * 0.22f, s * 0.06f, fill)
                canvas.drawCircle(cx + s * 0.2f, cy - s * 0.2f, s * 0.06f, fill)
                canvas.drawCircle(cx + s * 0.12f, cy - s * 0.22f, s * 0.06f, fill)
            }
            "curse" -> {
                canvas.drawCircle(cx, cy, s * 0.22f, line)
                canvas.drawLine(cx - s * 0.14f, cy - s * 0.14f, cx + s * 0.14f, cy + s * 0.14f, line)
                canvas.drawLine(cx + s * 0.14f, cy - s * 0.14f, cx - s * 0.14f, cy + s * 0.14f, line)
            }
            else -> { // teardrop
                val p = Path().apply {
                    moveTo(cx, cy - s * 0.28f)
                    cubicTo(cx + s * 0.18f, cy - s * 0.12f, cx + s * 0.18f, cy + s * 0.08f, cx, cy + s * 0.22f)
                    cubicTo(cx - s * 0.18f, cy + s * 0.08f, cx - s * 0.18f, cy - s * 0.12f, cx, cy - s * 0.28f)
                    close()
                }
                canvas.drawPath(p, fill)
            }
        }
    }

    /** 类型基础色 + 每条目色相微调 */
    fun colorFor(tags: String, name: String = ""): Int {
        val base = when {
            tags.contains("被动") -> floatArrayOf(0.58f, 0.55f, 0.85f)
            tags.contains("主动") -> floatArrayOf(0.02f, 0.65f, 0.85f)
            tags.contains("饰品") -> floatArrayOf(0.33f, 0.55f, 0.75f)
            tags.contains("塔罗") || tags.contains("符文") || tags.contains("卡牌") -> floatArrayOf(0.75f, 0.5f, 0.8f)
            tags.contains("胶囊") -> floatArrayOf(0.08f, 0.7f, 0.85f)
            tags.contains("堕化") -> floatArrayOf(0.98f, 0.45f, 0.7f)
            tags.contains("角色") -> floatArrayOf(0.12f, 0.6f, 0.85f)
            tags.contains("头目") -> floatArrayOf(0.97f, 0.5f, 0.75f)
            tags.contains("怪物") -> floatArrayOf(0.09f, 0.4f, 0.7f)
            tags.contains("跟班") -> floatArrayOf(0.5f, 0.5f, 0.7f)
            tags.contains("掉落") -> floatArrayOf(0.13f, 0.7f, 0.85f)
            tags.contains("房间") -> floatArrayOf(0.6f, 0.15f, 0.7f)
            tags.contains("章节") -> floatArrayOf(0.72f, 0.45f, 0.75f)
            tags.contains("障碍") -> floatArrayOf(0.07f, 0.35f, 0.7f)
            tags.contains("成就") -> floatArrayOf(0.4f, 0.5f, 0.7f)
            tags.contains("挑战") -> floatArrayOf(0.08f, 0.65f, 0.85f)
            tags.contains("种子") -> floatArrayOf(0.5f, 0.1f, 0.75f)
            tags.contains("机制") -> floatArrayOf(0.5f, 0.08f, 0.7f)
            tags.contains("状态") || tags.contains("诅咒") -> floatArrayOf(0.48f, 0.55f, 0.8f)
            tags.contains("套装") -> floatArrayOf(0.15f, 0.55f, 0.8f)
            else -> floatArrayOf(0.02f, 0.6f, 0.85f)
        }
        // 按名称哈希微调色相，保证每个词条图片各不相同
        val shift = (name.hashCode() % 24) / 24f * 0.12f - 0.06f
        val hsv = floatArrayOf(((base[0] + shift) % 1f + 1f) % 1f, base[1], base[2])
        return Color.HSVToColor(hsv)
    }
}
