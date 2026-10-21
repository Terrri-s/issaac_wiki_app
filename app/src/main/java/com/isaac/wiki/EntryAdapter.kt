package com.isaac.wiki

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** 条目列表适配器（带图标） */
class EntryAdapter(
    private val items: List<WikiEntry>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<EntryAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val name: TextView = view.findViewById(R.id.tv_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wiki_entry, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.icon.setImageBitmap(
            WikiRepository.icon(holder.itemView.context, item)
        )
        // 缩略图只显示中文名（去掉 " / English" 后缀）
        holder.name.text = item.name.split(" / ")[0].trim()
        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = items.size
}
