package ru.zarichan.guess_game

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ru.zarichan.guess_game.adapters.leaderboard.EazyLeaderboardAdapter
import ru.zarichan.guess_game.adapters.leaderboard.LeaderboardItem
import java.util.Collections


class LeaderboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedRef: SharedPreferences
    private lateinit var leaderboardRef: SharedPreferences

    private lateinit var itemsName: TextView
    private lateinit var itemsTime: TextView
    private lateinit var itemsAttempts: TextView

    private var difficulty: GameDifficulty = GameDifficulty.Normal


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        itemsName = findViewById(R.id.items__name)
        itemsTime = findViewById(R.id.items__time)
        itemsAttempts = findViewById(R.id.items__attemps)

        sharedRef = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        difficulty = GameDifficulty.values()[sharedRef.getInt("difficultyId", 1)]

        leaderboardRef = getSharedPreferences("LEADERBOARD", Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.litems__recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        recyclerView.adapter = CustomRecyclerAdapter(fillList())
        recyclerView.adapter = EazyLeaderboardAdapter(getLeadersList(difficulty))

        refreshLeaderboardHeader()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(
            when (difficulty) {
                GameDifficulty.Easy -> R.id.menu_0
                GameDifficulty.Normal -> R.id.menu_1
                GameDifficulty.Hard -> R.id.menu_2
            }
        ).isChecked = true
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = true

        when (item.title) {
            getText(R.string.menu_main_easy) -> difficulty = GameDifficulty.Easy
            getText(R.string.menu_main_normal) -> difficulty = GameDifficulty.Normal
            getText(R.string.menu_main_hard) -> difficulty = GameDifficulty.Hard
        }

        sharedRef
            .edit()
            .putInt("difficultyId", difficulty.ordinal)
            .apply();

        recyclerView.adapter = EazyLeaderboardAdapter(getLeadersList(difficulty))

        refreshLeaderboardHeader()

        return super.onOptionsItemSelected(item)
    }


    private fun getLeadersList(dif: GameDifficulty): Array<LeaderboardItem> {
        val key = when (dif) {
            GameDifficulty.Hard -> "leaders__hard"
            GameDifficulty.Normal -> "leaders__normal"
            GameDifficulty.Easy -> "leaders__easy"
        }

        val data = StringBuilder()

        try {
            val fileName = "SAVE__$key"

            val fis = openFileInput(fileName)
            val input = ByteArray(fis.available())

            while (fis.read(input) >= 0) data.append(String(input))
            fis.close()

            Toast.makeText(
                applicationContext,
                "Файл $fileName открыт", Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.d("DEBUG_TTT", "Cant load file - $e")
            return if (leaderboardRef.contains(key)) {
                val refData: String = leaderboardRef.getString(key, "")!!
                Gson().fromJson(refData, Array<LeaderboardItem>::class.java)
            } else {
                arrayOf()
            }
        }

        val leaders = Gson().fromJson(data.toString(), Array<LeaderboardItem>::class.java)

        leaders.sortBy { it.attempts }

        return leaders
    }


    fun toMain(view: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }



    fun refreshLeaderboardHeader() {
        when (difficulty) {
            GameDifficulty.Easy -> {
                itemsName.isInvisible = false
                itemsTime.isInvisible = true
                itemsAttempts.isInvisible = true
            }
            GameDifficulty.Normal -> {
                itemsName.isInvisible = false
                itemsTime.isInvisible = true
                itemsAttempts.isInvisible = false
            }
            GameDifficulty.Hard -> {
                itemsName.isInvisible = false
                itemsTime.isInvisible = false
                itemsAttempts.isInvisible = false
            }
        }
    }
}