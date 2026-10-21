package com.isaac.wiki

import android.app.Activity
import android.app.Application
import android.os.Bundle

/** 应用级入口：全局背景音乐跟随前后台自动播放/暂停 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        BgmManager.init(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) = BgmManager.onActivityStarted()
            override fun onActivityStopped(activity: Activity) = BgmManager.onActivityStopped()
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
