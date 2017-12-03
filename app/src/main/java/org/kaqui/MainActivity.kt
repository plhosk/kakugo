package org.kaqui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.kaqui.model.KaquiDb
import org.kaqui.model.QuizzType
import org.kaqui.model.parseFile
import org.kaqui.settings.KanaSelectionActivity
import org.kaqui.settings.KanjiSelectionActivity
import java.io.Serializable
import java.util.zip.GZIPInputStream

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private enum class Mode {
        MAIN,
        HIRAGANA,
        KATAKANA,
        KANJI,
    }

    private var initProgress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        hiragana_quizz.transformationMethod = null
        katakana_quizz.transformationMethod = null
        kanji_quizz.transformationMethod = null

        hiragana_quizz.setOnClickListener { setMode(Mode.HIRAGANA) }
        katakana_quizz.setOnClickListener { setMode(Mode.KATAKANA) }
        kanji_quizz.setOnClickListener { setMode(Mode.KANJI) }
        start_hiragana_to_romaji_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.HIRAGANA_TO_ROMAJI)))
        start_romaji_to_hiragana_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.ROMAJI_TO_HIRAGANA)))
        start_katakana_to_romaji_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.KATAKANA_TO_ROMAJI)))
        start_romaji_to_katakana_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.ROMAJI_TO_KATAKANA)))
        start_kanji_reading_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.KANJI_TO_READING)))
        start_reading_kanji_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.READING_TO_KANJI)))
        start_kanji_meaning_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.KANJI_TO_MEANING)))
        start_meaning_kanji_quizz.setOnClickListener(View.OnClickListener(makeQuizzLauncher(QuizzType.MEANING_TO_KANJI)))

        hiragana_selection_button.setOnClickListener {
            startActivity(Intent(this, KanaSelectionActivity::class.java).putExtra("mode", KanaSelectionActivity.Mode.HIRAGANA as Serializable))
        }
        katakana_selection_button.setOnClickListener {
            startActivity(Intent(this, KanaSelectionActivity::class.java).putExtra("mode", KanaSelectionActivity.Mode.KATAKANA as Serializable))
        }
        kanji_selection_button.setOnClickListener {
            startActivity(Intent(this, KanjiSelectionActivity::class.java))
        }

        setMode(Mode.MAIN)
    }

    private fun setMode(mode: Mode) {
        main_layout.visibility = if (mode == Mode.MAIN) View.VISIBLE else View.GONE
        hiragana_layout.visibility = if (mode == Mode.HIRAGANA) View.VISIBLE else View.GONE
        katakana_layout.visibility = if (mode == Mode.KATAKANA) View.VISIBLE else View.GONE
        kanji_layout.visibility = if (mode == Mode.KANJI) View.VISIBLE else View.GONE

        if (mode == Mode.KANJI) {
            val db = KaquiDb.getInstance(this)
            if (db.empty) {
                showDownloadProgressDialog()
                async(CommonPool) {
                    initKanjiDic()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (main_layout.visibility != View.VISIBLE)
            setMode(Mode.MAIN)
        else
            super.onBackPressed()
    }

    private fun makeQuizzLauncher(type: QuizzType): (View) -> Unit {
        return {
            val db = KaquiDb.getInstance(this)
            if (QuizzActivity.getItemView(db, type).getEnabledCount() < 10) {
                Toast.makeText(this, R.string.enable_a_few_items, Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, QuizzActivity::class.java)
                intent.putExtra("quizz_type", type)
                startActivity(intent)
            }
        }
    }

    private fun showDownloadProgressDialog() {
        initProgress = ProgressDialog(this)
        initProgress!!.setMessage(getString(R.string.initializing_kanji_db))
        initProgress!!.setCancelable(false)
        initProgress!!.show()
    }

    private fun initKanjiDic() {
        try {
            resources.openRawResource(R.raw.kanjidic).use { gzipStream ->
                GZIPInputStream(gzipStream, 1024).use { textStream ->
                    val db = KaquiDb.getInstance(this)
                    val dump = db.dumpUserData()
                    db.replaceKanjis(parseFile(textStream.bufferedReader()))
                    db.restoreUserData(dump)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize kanji database", e)
            async(UI) {
                Toast.makeText(this@MainActivity, getString(R.string.failed_to_init_kanji_db, e.message), Toast.LENGTH_LONG).show()
            }
        }

        async(UI) {
            initProgress!!.dismiss()
            initProgress = null
        }
    }
}