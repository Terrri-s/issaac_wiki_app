package com.isaac.wiki

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** 分类列表适配器 */
class CategoryAdapter(
    private val items: List<WikiCategory>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val index: TextView = view.findViewById(R.id.tv_index)
        val name: TextView = view.findViewById(R.id.tv_name)
        val sub: TextView = view.findViewById(R.id.tv_sub)
        val count: TextView = view.findViewById(R.id.tv_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wiki_category, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.index.text = (position + 1).toString()
        holder.name.text = item.name
        holder.sub.text = item.subtitle
        holder.count.text = holder.itemView.context.getString(R.string.count_format, item.entries.size)
        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = items.size
}
