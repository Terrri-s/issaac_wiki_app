package com.isaac.wiki

import android.content.Context
import android.media.MediaPlayer

/**
 * 全局背景音乐管理：跨页面持续播放，切后台自动暂停，开关状态持久化。
 */
object BgmManager {

    private var player: MediaPlayer? = null
    private var enabled = true
    private var startedCount = 0
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        enabled = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("music_on", true)
        player = try {
            app.assets.openFd("music/bgm_title.mp3").use { afd ->
                MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    isLooping = true
                    setVolume(0.55f, 0.55f)
                    prepare()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isEnabled(): Boolean = enabled

    fun setEnabled(context: Context, on: Boolean) {
        enabled = on
        context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putBoolean("music_on", on).apply()
        if (on) {
            if (startedCount > 0 && player?.isPlaying != true) player?.start()
        } else {
            player?.pause()
        }
    }

    /** 任意页面进入前台（含页面间跳转） */
    fun onActivityStarted() {
        startedCount++
        if (enabled && player?.isPlaying != true) player?.start()
    }

    /** 全部页面退到后台时暂停 */
    fun onActivityStopped() {
        startedCount = (startedCount - 1).coerceAtLeast(0)
        if (startedCount == 0) player?.pause()
    }
}
