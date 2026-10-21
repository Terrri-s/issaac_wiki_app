package com.isaac.wiki

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 主页适配器：首行为艺术字 Logo 头（跨两列），随后是分类按钮网格
 */
class HomeAdapter(
    private val categories: List<WikiCategory>,
    private val onCategory: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /** 已播放过入场动画的卡片位置 */
    private val animated = mutableSetOf<Int>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CATEGORY = 1

        /** 各分类按钮的代表性词条（取该词条的游戏图片作为按钮图标） */
        private val REPRESENTATIVE = mapOf(
            "人物" to "以撒", "属性" to "伤害", "效果" to "中毒", "道具" to "悲伤洋葱",
            "掉落" to "红心", "卡牌" to "愚者", "胶囊" to "剧毒臭屁", "套装" to "咕噜套装",
            "饰品" to "被吞下的硬币", "房间" to "宝箱房", "章节" to "地下室", "障碍物" to "岩石",
            "成就" to "弑神大神", "挑战" to "漆黑一片", "怪物" to "苍蝇", "头目" to "萌死戳",
            "跟班" to "蓝苍蝇", "种子" to "种子机制", "机制" to "道具池"
        )
    }

    class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.iv_logo)
        val tvLogo: TextView = view.findViewById(R.id.tv_logo)
    }

    class CategoryHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val name: TextView = view.findViewById(R.id.tv_name)
        val count: TextView = view.findViewById(R.id.tv_count)
    }

    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_HEADER else TYPE_CATEGORY

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderHolder(inflater.inflate(R.layout.item_home_header, parent, false))
        } else {
            CategoryHolder(inflater.inflate(R.layout.item_home_category, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderHolder) {
            bindHeader(holder)
            return
        }
        val h = holder as CategoryHolder
        val cat = categories[position - 1]
        h.icon.setImageBitmap(representativeIcon(h, cat))
        h.name.text = cat.name
        h.count.text = h.itemView.context.getString(R.string.count_format, cat.entries.size)
        h.itemView.setOnClickListener {
            // 点击回弹后再跳转（游戏风格的 Q 弹手感）
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(140)
                    .setInterpolator(android.view.animation.OvershootInterpolator(2f))
                    .withEndAction { onCategory(position - 1) }.start()
            }.start()
        }
        // 卡片入场动画：每个位置只播放一次，交错淡入上浮
        if (animated.add(position)) {
            h.itemView.alpha = 0f
            h.itemView.translationY = 48f
            h.itemView.animate()
                .alpha(1f).translationY(0f)
                .setDuration(320)
                .setStartDelay((position * 55).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    /** 头行：加载艺术字 Logo（缺失时回退文字） */
    private fun bindHeader(h: HeaderHolder) {
        val ctx = h.itemView.context
        val bmp = try {
            ctx.assets.open("logo.png").use {
                android.graphics.BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
        if (bmp != null) {
            h.logo.setImageBitmap(bmp)
            h.logo.visibility = View.VISIBLE
            h.tvLogo.visibility = View.GONE
            // Logo 轻轻上下浮动
            if (h.logo.tag == null) {
                h.logo.tag = true
                android.animation.ObjectAnimator.ofFloat(h.logo, "translationY", 0f, -12f, 0f).apply {
                    duration = 2600
                    repeatCount = android.animation.ValueAnimator.INFINITE
                    interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                    start()
                }
            }
        } else {
            h.logo.visibility = View.GONE
            h.tvLogo.visibility = View.VISIBLE
        }
    }

    /** 分类按钮图标：取代表性词条的真实游戏图片 */
    private fun representativeIcon(h: CategoryHolder, cat: WikiCategory): android.graphics.Bitmap? {
        val ctx = h.itemView.context
        val zh = REPRESENTATIVE[cat.name]
        val entry = zh?.let { z -> cat.entries.firstOrNull { it.name.split(" / ")[0].contains(z) } }
            ?: cat.entries.firstOrNull()
        if (entry != null) {
            WikiRepository.icon(ctx, entry)?.let { return it }
        }
        return ItemIconGenerator.icon(ctx, cat.name, cat.subtitle)
    }

    override fun getItemCount(): Int = categories.size + 1

    /** 头行跨两列 */
    fun setSpan(grid: androidx.recyclerview.widget.GridLayoutManager) {
        grid.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (position == 0) grid.spanCount else 1
        }
    }
}
