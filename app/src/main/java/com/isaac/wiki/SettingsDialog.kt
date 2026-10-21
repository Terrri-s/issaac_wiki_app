package com.isaac.wiki

import android.app.Activity
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

/** 设置小窗口：点击设置按钮弹出/收起，内置背景音乐开关 */
object SettingsDialog {

    fun show(activity: Activity) {
        val density = activity.resources.displayMetrics.density
        fun dp(v: Int): Int = (v * density).toInt()

        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(14), dp(20), dp(6))
        }
        val label = TextView(activity).apply {
            text = "♪ 背景音乐"
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(activity, R.color.text_primary))
        }
        val sw = SwitchCompat(activity).apply {
            isChecked = BgmManager.isEnabled()
        }
        row.addView(label, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(sw)

        val dialog = AlertDialog.Builder(activity)
            .setTitle("设置")
            .setView(row)
            .setPositiveButton("完成", null)
            .create()
        sw.setOnCheckedChangeListener { _, on -> BgmManager.setEnabled(activity, on) }
        dialog.show()
    }
}
