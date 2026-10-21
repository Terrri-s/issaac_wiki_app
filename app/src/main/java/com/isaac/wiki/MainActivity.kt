package com.isaac.wiki

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var homeAdapter: HomeAdapter
    private val categories by lazy { WikiRepository.categories(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        recycler = findViewById(R.id.recycler)
        tvEmpty = findViewById(R.id.tv_empty)

        val grid = GridLayoutManager(this, 2)
        homeAdapter = HomeAdapter(categories) { index ->
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("categoryIndex", index)
            startActivity(intent)
        }
        homeAdapter.setSpan(grid)
        recycler.layoutManager = grid
        recycler.adapter = homeAdapter

        findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                search(s?.toString().orEmpty())
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            SettingsDialog.show(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** 模糊搜索标题（中英文皆可），空串时回到分类首页 */
    private fun search(query: String) {
        val q = query.trim().lowercase().replace(" ", "")
        if (q.isEmpty()) {
            tvEmpty.visibility = View.GONE
            val grid = GridLayoutManager(this, 2)
            homeAdapter.setSpan(grid)
            recycler.layoutManager = grid
            recycler.adapter = homeAdapter
            return
        }

        val hits = mutableListOf<Triple<Int, Int, WikiEntry>>()
        for ((ci, cat) in categories.withIndex()) {
            for ((ei, entry) in cat.entries.withIndex()) {
                if (matches(entry.name.lowercase().replace(" ", ""), q)) {
                    hits.add(Triple(ci, ei, entry))
                }
            }
        }

        tvEmpty.visibility = if (hits.isEmpty()) View.VISIBLE else View.GONE
        recycler.layoutManager = GridLayoutManager(this, 4)
        recycler.adapter = EntryAdapter(hits.map { it.third }) { position ->
            val (ci, ei, _) = hits[position]
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("categoryIndex", ci)
            intent.putExtra("entryIndex", ei)
            startActivity(intent)
        }
    }

    /** 模糊匹配：子串命中，或查询字符按顺序出现 */
    private fun matches(name: String, q: String): Boolean {
        if (name.contains(q)) return true
        var i = 0
        for (c in q) {
            i = name.indexOf(c, i)
            if (i < 0) return false
            i++
        }
        return true
    }
}
