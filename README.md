# 以撒的结合 Wiki（Android）

《以撒的结合：重生 / 忏悔》非官方离线图鉴 App，覆盖游戏全部词条内容，纯本地数据、无网络也可使用。

## 功能特性

- **全量词条**：19 个分类、2406+ 条词条（人物 34、道具 719、饰品 188、成就 641、挑战 45、怪物 367、头目 103 等），全部配真实游戏素材图（2790+ 张）
- **词条互链**：正文中首次出现的词条名可点击跳转，道具 / 角色 / 饰品 / 套装 / 怪物 / 成就全品类互相关联；同名歧义（如「？？？」胶囊 vs 角色）自动按分类优先级消歧
- **角色通关路线图**：按通关 Boss 路线分组的图表展示，成就 icon 节点与解锁道具均可点击跳转
- **道具序号与控制台用法**：按 wiki 道具 ID 标注序号，并附 `spawn 5.100.<ID>` 控制台指令用法
- **基础描述高亮**：道具 / 饰品 / 卡牌 / 胶囊 / 跟班 / 效果的基础描述以醒目字体与颜色展示
- **全局背景音乐**：音乐开关独立于可显示/隐藏的设置小窗中，状态持久化，切后台自动暂停
- **浏览与搜索**：分类网格浏览（一行四个）、首页模糊搜索（中英文）

## 技术栈

- Kotlin + AndroidX + Material Components
- RecyclerView（GridLayoutManager 网格列表）
- SpannableString / ClickableSpan 实现正文互链
- 数据离线存储于 `app/src/main/assets/wiki_data.json`，配图位于 `app/src/main/assets/img/`

## 构建

环境要求：JDK 17+、Android SDK 34、Gradle 8.2

```bash
./gradlew assembleDebug
```

产物位于 `app/build/outputs/apk/debug/app-debug.apk`，直接安装即可（minSdk 24 / targetSdk 34）。

## 项目结构

```
isaac-wiki/
├── app/src/main/java/com/isaac/wiki/   # Kotlin 源码
│   ├── MainActivity.kt                 # 首页（分类导航 + 搜索）
│   ├── CategoryActivity.kt             # 分类网格列表
│   ├── DetailActivity.kt               # 词条详情（互链 / 路线图 / 高亮）
│   ├── WikiRepository.kt / WikiModels.kt  # 数据加载与互链检索
│   ├── SettingsDialog.kt / BgmManager.kt  # 设置小窗与全局背景音乐
│   ├── HomeAdapter.kt / CategoryAdapter.kt / EntryAdapter.kt
│   └── ItemIconGenerator.kt / ParticleView.kt / App.kt
├── app/src/main/assets/                # 离线图鉴数据与游戏素材配图
└── app/src/main/res/                   # 布局 / 图标 / 菜单资源
```

## 版权说明

本项目为粉丝向非官方图鉴，游戏素材图片版权归 Nicalis / Edmund McMillen 所有，仅供学习交流使用。
