package org.kaqui_plhosk.settings

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import org.kaqui_plhosk.BaseActivity
import org.kaqui_plhosk.R
import org.kaqui_plhosk.StatsFragment
import org.kaqui_plhosk.databinding.ItemSelectionActivityBinding
import org.kaqui_plhosk.model.Database
import org.kaqui_plhosk.model.LearningDbView

class ItemSearchActivity : BaseActivity() {
    private lateinit var dbView: LearningDbView
    private lateinit var listAdapter: ItemSelectionAdapter
    private lateinit var statsFragment: StatsFragment
    private lateinit var mode: Mode

    enum class Mode {
        KANJI,
        WORD,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mode = intent.getSerializableExtra("mode") as Mode

        val binding = ItemSelectionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        statsFragment = StatsFragment.newInstance()
        statsFragment.setShowDisabled(true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.global_stats, statsFragment)
                .commit()

        dbView = when (mode) {
            Mode.KANJI -> Database.getInstance(this).getKanjiView()
            Mode.WORD -> Database.getInstance(this).getWordView()
        }

        listAdapter = ItemSelectionAdapter(dbView, this, statsFragment)
        binding.itemList.adapter = listAdapter
        binding.itemList.layoutManager = LinearLayoutManager(this)

        val searchView = SearchView(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Dark))
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchKanjiList(newText)
                return true
            }
        })
        // never collapse the searchView
        searchView.setOnCloseListener { true }
        searchView.isIconified = false
        if (mode == Mode.KANJI)
            searchView.queryHint = resources.getString(R.string.search_kanji_hint)
        else
            searchView.queryHint = resources.getString(R.string.search_word_hint)
        supportActionBar!!.customView = searchView
        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_HOME_AS_UP
        searchView.requestFocusFromTouch()
    }

    override fun onStart() {
        super.onStart()

        statsFragment.updateStats(dbView)
    }

    private fun searchKanjiList(str: String) {
        if (str.isEmpty())
            listAdapter.clearAll()
        else
            listAdapter.searchFor(str)
    }

    companion object {
        private const val TAG = "ItemSearchActivity"
    }
}
