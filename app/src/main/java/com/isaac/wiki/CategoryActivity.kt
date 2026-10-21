package com.isaac.wiki

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryActivity : AppCompatActivity() {

    private lateinit var category: WikiCategory
    private var allEntries: List<WikiEntry> = emptyList()
    private lateinit var adapter: EntryAdapter
    private lateinit var countView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val index = intent.getIntExtra("categoryIndex", 0)
        category = WikiRepository.categories(this).getOrElse(index) { WikiCategory("未知", "", emptyList()) }
        supportActionBar?.title = category.name
        allEntries = category.entries

        countView = findViewById(R.id.tv_count)
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 4)
        adapter = EntryAdapter(allEntries) { pos ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("categoryIndex", index)
            intent.putExtra("entryIndex", pos)
            startActivity(intent)
        }
        recycler.adapter = adapter
        updateCount(allEntries.size)

        findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                val filtered = if (q.isEmpty()) allEntries else allEntries.filter {
                    it.name.contains(q, ignoreCase = true) ||
                        it.desc.contains(q, ignoreCase = true) ||
                        it.tags.contains(q, ignoreCase = true) ||
                        it.detail.contains(q, ignoreCase = true)
                }
                adapter = EntryAdapter(filtered) { pos ->
                    val intent = Intent(this@CategoryActivity, DetailActivity::class.java)
                    intent.putExtra("categoryIndex", index)
                    // 搜索结果使用过滤后的索引：直接传入过滤列表下标
                    intent.putExtra("entryIndex", filtered[pos].let { allEntries.indexOf(it) })
                    startActivity(intent)
                }
                findViewById<RecyclerView>(R.id.recycler).adapter = adapter
                updateCount(filtered.size)
            }
        })
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

    private fun updateCount(size: Int) {
        countView.text = getString(R.string.count_format, size)
    }
}
