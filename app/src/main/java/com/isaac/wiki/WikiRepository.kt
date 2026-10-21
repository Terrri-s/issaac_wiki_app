package com.isaac.wiki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import org.json.JSONArray
import org.json.JSONObject

/**
 * 从 assets 加载本地图鉴数据（离线可用），并提供互链检索
 */
object WikiRepository {

    @Volatile
    private var cache: List<WikiCategory>? = null

    fun categories(context: Context): List<WikiCategory> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val raw = context.assets.open("wiki_data.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(raw)
            val arr: JSONArray = root.getJSONArray("categories")
            val list = mutableListOf<WikiCategory>()
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                val entriesArr = c.getJSONArray("entries")
                val entries = mutableListOf<WikiEntry>()
                for (j in 0 until entriesArr.length()) {
                    val e = entriesArr.getJSONObject(j)
                    val links = mutableListOf<WikiLink>()
                    if (e.has("links")) {
                        val linksArr = e.getJSONArray("links")
                        for (k in 0 until linksArr.length()) {
                            val l = linksArr.getJSONObject(k)
                            links.add(
                                WikiLink(
                                    category = l.getString("category"),
                                    entry = l.getString("entry")
                                )
                            )
                        }
                    }
                    entries.add(
                        WikiEntry(
                            name = e.getString("name"),
                            tags = e.optString("tags", ""),
                            desc = e.optString("desc", ""),
                            detail = e.optString("detail", ""),
                            img = e.optString("img", ""),
                            links = links,
                            routes = parseRoutes(e)
                        )
                    )
                }
                list.add(
                    WikiCategory(
                        name = c.getString("name"),
                        subtitle = c.optString("subtitle", ""),
                        entries = entries
                    )
                )
            }
            cache = list
            return list
        }
    }

    /** 解析条目的通关路线图表数据（人物词条） */
    private fun parseRoutes(e: JSONObject): List<RouteGroup> {
        if (!e.has("routes")) return emptyList()
        val out = mutableListOf<RouteGroup>()
        val arr = e.getJSONArray("routes")
        for (i in 0 until arr.length()) {
            val g = arr.getJSONObject(i)
            val achs = mutableListOf<RouteAch>()
            val aa = g.getJSONArray("achs")
            for (j in 0 until aa.length()) {
                val a = aa.getJSONObject(j)
                val rewards = mutableListOf<String>()
                if (a.has("rewards")) {
                    val ra = a.getJSONArray("rewards")
                    for (k in 0 until ra.length()) rewards.add(ra.getString(k))
                }
                achs.add(RouteAch(a.getString("ach"), rewards))
            }
            out.add(RouteGroup(g.getString("route"), achs))
        }
        return out
    }

    /** 按 (分类名, 条目名) 查找条目位置，返回 Pair<分类索引, 条目索引> 或 null */
    fun find(context: Context, categoryName: String, entryName: String): Pair<Int, Int>? {
        val cats = categories(context)
        val ci = cats.indexOfFirst { it.name == categoryName }
        if (ci < 0) return null
        val ei = cats[ci].entries.indexOfFirst { it.name == entryName }
        if (ei < 0) return null
        return ci to ei
    }

    private val iconCache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    /** 词条图片：优先使用 assets/img 下的游戏素材图，缺失时回退到生成图标 */
    fun icon(context: Context, entry: WikiEntry): Bitmap? {
        val key = entry.img
        if (key.isNotEmpty()) {
            iconCache.get(key)?.let { return it }
            val bmp = try {
                context.assets.open("img/$key").use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
            if (bmp != null) {
                iconCache.put(key, bmp)
                return bmp
            }
        }
        return ItemIconGenerator.icon(context, entry.name, entry.tags)
    }
}
