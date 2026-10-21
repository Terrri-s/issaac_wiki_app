package com.isaac.wiki

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DetailActivity : AppCompatActivity() {

    /** 基础描述用显眼样式展示的分类 */
    private val highlightCats = setOf("道具", "饰品", "卡牌", "胶囊", "跟班", "效果")

    /** 同名歧义时的分类优先级（越小越优先，如"？？？"优先指向人物而非胶囊） */
    private val catOrder = listOf(
        "人物", "道具", "饰品", "跟班", "卡牌", "套装", "胶囊", "效果", "属性",
        "头目", "怪物", "成就", "挑战", "掉落", "房间", "章节", "障碍物", "种子", "机制"
    )

    private fun catPriority(name: String): Int =
        catOrder.indexOf(name).let { if (it < 0) 99 else it }

    /** 全角转半角 + 小写（1:1 等长，便于与正文逐位对齐） */
    private fun fold(s: String): String = buildString(s.length) {
        for (ch in s) {
            val o = ch.code
            append(
                when {
                    o in 0xFF01..0xFF5E -> (o - 0xFEE0).toChar().lowercaseChar()
                    ch == '\u3000' -> ' '
                    else -> ch.lowercaseChar()
                }
            )
        }
    }

    /** 归一化检索键（等长折叠后再去空格） */
    private fun normKey(s: String): String = fold(s).replace(" ", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_detail)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val categoryIndex = intent.getIntExtra("categoryIndex", 0)
        val entryIndex = intent.getIntExtra("entryIndex", 0)
        val categories = WikiRepository.categories(this)
        val category = categories.getOrElse(categoryIndex) { WikiCategory("未知", "", emptyList()) }
        val entry = category.entries.getOrElse(entryIndex) { WikiEntry("未知", "", "", "") }

        supportActionBar?.title = category.name
        val icon = findViewById<ImageView>(R.id.iv_icon)
        icon.setImageBitmap(WikiRepository.icon(this, entry))
        // 主图入场弹出 + 轻微漂浮
        icon.alpha = 0f
        icon.scaleX = 0.55f
        icon.scaleY = 0.55f
        icon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(340)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.6f)).start()
        android.animation.ObjectAnimator.ofFloat(icon, "translationY", 0f, -9f, 0f).apply {
            duration = 2800
            repeatCount = android.animation.ValueAnimator.INFINITE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            start()
        }
        findViewById<TextView>(R.id.tv_name).text = entry.name
        findViewById<TextView>(R.id.tv_tags).text = entry.tags

        renderDetail(entry, category.name, categoryIndex, entryIndex)
        renderRoutes(entry)
        renderLinks(entry)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                SettingsDialog.show(this)
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 渲染正文：
     * 1) 道具/饰品/卡牌等的基础描述用显眼字体与颜色
     * 2) 段落标题【xxx】用主题色加粗
     * 3) 正文中首次出现的词条名变为可点击跳转入口
     */
    private fun renderDetail(entry: WikiEntry, catName: String, catIndex: Int, entryIndex: Int) {
        val tv = findViewById<TextView>(R.id.tv_detail)
        val text = entry.detail
        if (text.isEmpty()) return
        val span = SpannableString(text)
        val accent = ContextCompat.getColor(this, R.color.accent_red)
        val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE

        // 段落标题【xxx】
        val headerRegex = Regex("【[^】]+】")
        for (m in headerRegex.findAll(text)) {
            span.setSpan(ForegroundColorSpan(accent), m.range.first, m.range.last + 1, flag)
            span.setSpan(StyleSpan(Typeface.BOLD), m.range.first, m.range.last + 1, flag)
        }

        // 基础描述（首个【】段落之前的文本）
        if (catName in highlightCats) {
            val end = text.indexOf("【").let { if (it < 0) text.length else it }
            if (end > 0) {
                span.setSpan(RelativeSizeSpan(1.18f), 0, end, flag)
                span.setSpan(StyleSpan(Typeface.BOLD), 0, end, flag)
                span.setSpan(ForegroundColorSpan(accent), 0, end, flag)
            }
        }

        // 正文中首次出现的可链接词条（按归一化名匹配，同名歧义只保留优先级最高的目标）
        val used = BooleanArray(text.length)
        val folded = fold(text)
        val candidates = linkCandidates(entry, catIndex, entryIndex)
        for ((term, ci, ei) in candidates) {
            if (term.length < 2) continue
            val start = folded.indexOf(fold(term))
            if (start < 0) continue
            val end = start + term.length
            if ((start until end).any { used[it] }) continue
            for (i in start until end) used[i] = true
            span.setSpan(EntryLink(ci, ei), start, end, flag)
            span.setSpan(UnderlineSpan(), start, end, flag)
            span.setSpan(ForegroundColorSpan(accent), start, end, flag)
        }

        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.text = span
    }

    /** 全库词条的可链接候选（中文名，分类索引，条目索引）。
     *  同名（全半角/大小写归一后相同）只保留分类优先级最高的一个目标，长名优先 */
    private fun linkCandidates(entry: WikiEntry, catIndex: Int, entryIndex: Int): List<Triple<String, Int, Int>> {
        val best = LinkedHashMap<String, Triple<String, Int, Int>>()
        val bestPrio = LinkedHashMap<String, Int>()
        for ((ci, cat) in WikiRepository.categories(this).withIndex()) {
            for ((ei, e) in cat.entries.withIndex()) {
                if (ci == catIndex && ei == entryIndex) continue
                val zh = e.name.split(" / ")[0].trim()
                if (zh.length < 2) continue
                val key = normKey(zh)
                val prio = catPriority(cat.name)
                if (best[key] == null || prio < (bestPrio[key] ?: 99)) {
                    best[key] = Triple(zh, ci, ei)
                    bestPrio[key] = prio
                }
            }
        }
        return best.values.sortedByDescending { it.first.length }
    }

    /** 人物词条：通关路线图表（路线分组 + 成就icon节点 + 解锁道具链接） */
    private fun renderRoutes(entry: WikiEntry) {
        if (entry.routes.isEmpty()) return
        val container = findViewById<LinearLayout>(R.id.routes_container)
        container.visibility = View.VISIBLE
        val accent = ContextCompat.getColor(this, R.color.accent_red)

        // 图表总标题
        container.addView(TextView(this).apply {
            text = "【通关路线图】"
            setTextColor(accent)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
        })

        for (group in entry.routes) {
            // 路线标题
            val title = TextView(this).apply {
                text = "◆ ${group.route}"
                setTextColor(accent)
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
            }
            val titleParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            titleParams.setMargins(0, dp(12), 0, dp(4))
            container.addView(title, titleParams)

            // 成就节点横向图表
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            for ((idx, node) in group.achs.withIndex()) {
                row.addView(buildAchCard(node), achCardParams(idx))
            }
            val scroll = HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
                addView(row)
            }
            container.addView(scroll)
        }
    }

    /** 单个成就节点卡片：成就icon + 名称 + 解锁奖励（均可点击跳转） */
    private fun buildAchCard(node: RouteAch): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundResource(R.drawable.bg_chip)
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }

        val icon = ImageView(this).apply {
            val found = WikiRepository.find(this@DetailActivity, "成就", node.ach)
            val bmp = found?.let {
                val cats = WikiRepository.categories(this@DetailActivity)
                WikiRepository.icon(this@DetailActivity, cats[it.first].entries[it.second])
            }
            if (bmp != null) setImageBitmap(bmp)
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
        }
        card.addView(icon)

        val name = TextView(this).apply {
            text = node.ach.split(" / ")[0]
            setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.text_primary))
            textSize = 11f
            gravity = Gravity.CENTER
            maxWidth = dp(92)
        }
        card.addView(name)

        for (reward in node.rewards) {
            val rw = TextView(this).apply {
                text = "→ ${reward.split(" / ")[0]}"
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.accent_red))
                textSize = 11f
                gravity = Gravity.CENTER
                maxWidth = dp(92)
                setOnClickListener { jumpAny(reward) }
            }
            card.addView(rw)
        }

        card.setOnClickListener { jump("成就", node.ach) }
        return card
    }

    private fun achCardParams(idx: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        p.setMargins(if (idx == 0) 0 else dp(8), 0, 0, 0)
        return p
    }

    /** 跳转到指定词条，成功返回 true */
    private fun jump(category: String, entry: String): Boolean? {
        val found = WikiRepository.find(this, category, entry) ?: return null
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("categoryIndex", found.first)
        intent.putExtra("entryIndex", found.second)
        startActivity(intent)
        return true
    }

    /** 不限分类，按条目名跳转（用于奖励词条）；同名歧义取分类优先级最高的目标 */
    private fun jumpAny(entry: String) {
        val key = normKey(entry.split(" / ")[0].trim())
        var best: Pair<Int, Int>? = null
        var bestPrio = 99
        for ((ci, cat) in WikiRepository.categories(this).withIndex()) {
            for ((ei, e) in cat.entries.withIndex()) {
                if (normKey(e.name.split(" / ")[0].trim()) != key) continue
                val prio = catPriority(cat.name)
                if (best == null || prio < bestPrio) {
                    best = ci to ei
                    bestPrio = prio
                }
            }
        }
        val found = best ?: return
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("categoryIndex", found.first)
        intent.putExtra("entryIndex", found.second)
        startActivity(intent)
    }

    /** 渲染互链词条，点击即可跳转到对应条目详情 */
    private fun renderLinks(entry: WikiEntry) {
        if (entry.links.isEmpty()) return
        findViewById<TextView>(R.id.tv_links_title).visibility = View.VISIBLE
        val container = findViewById<LinearLayout>(R.id.links_container)
        for (link in entry.links) {
            val chip = TextView(this).apply {
                text = "${link.category} · ${link.entry}"
                setBackgroundResource(R.drawable.bg_chip)
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.text_primary))
                textSize = 13f
                setPadding(dp(12), dp(8), dp(12), dp(8))
                gravity = Gravity.CENTER_VERTICAL
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, dp(6), 0, 0)
            chip.layoutParams = params
            // 关联词条交错滑入
            chip.alpha = 0f
            chip.translationX = 28f
            chip.animate().alpha(1f).translationX(0f).setDuration(260)
                .setStartDelay((container.childCount * 45).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
            chip.setOnClickListener {
                val found = WikiRepository.find(this, link.category, link.entry)
                if (found != null) {
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra("categoryIndex", found.first)
                    intent.putExtra("entryIndex", found.second)
                    startActivity(intent)
                }
            }
            container.addView(chip)
        }
    }

    /** 正文内词条链接点击跳转 */
    private inner class EntryLink(val ci: Int, val ei: Int) : ClickableSpan() {
        override fun onClick(widget: View) {
            val intent = Intent(this@DetailActivity, DetailActivity::class.java)
            intent.putExtra("categoryIndex", ci)
            intent.putExtra("entryIndex", ei)
            startActivity(intent)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
