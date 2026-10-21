package com.isaac.wiki

/** 词条互链引用 */
data class WikiLink(
    val category: String,
    val entry: String
)

/** 通关路线中的单个成就节点 */
data class RouteAch(
    val ach: String,
    val rewards: List<String>
)

/** 通关路线分组（路线名 -> 成就节点列表） */
data class RouteGroup(
    val route: String,
    val achs: List<RouteAch>
)

/** 图鉴条目 */
data class WikiEntry(
    val name: String,
    val tags: String,
    val desc: String,
    val detail: String,
    val img: String = "",
    val links: List<WikiLink> = emptyList(),
    val routes: List<RouteGroup> = emptyList()
)

/** 图鉴分类 */
data class WikiCategory(
    val name: String,
    val subtitle: String,
    val entries: List<WikiEntry>
)
