package ru.zarichan.change_scene

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ru.zarichan.change_scene.adapters.leaderboard.EazyLeaderboardAdapter
import ru.zarichan.change_scene.adapters.leaderboard.LeaderboardItem

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedRef: SharedPreferences
    private lateinit var leaderboardRef: SharedPreferences

    private var difficulty: GameDifficulty = GameDifficulty.Normal


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        sharedRef = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        difficulty = GameDifficulty.values()[sharedRef.getInt("difficultyId", 1)]

        leaderboardRef = getSharedPreferences("LEADERBOARD", Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.litems__recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        recyclerView.adapter = CustomRecyclerAdapter(fillList())
        recyclerView.adapter = EazyLeaderboardAdapter(getLeadersList(difficulty))
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

        return super.onOptionsItemSelected(item)
    }


    private fun getLeadersList(dif: GameDifficulty): Array<LeaderboardItem> {
        val key = when (dif) {
            GameDifficulty.Hard -> "leaders__hard"
            GameDifficulty.Normal -> "leaders__normal"
            GameDifficulty.Easy -> "leaders__easy"
        }
        val data: String = leaderboardRef.getString(key, null) ?: ""
        return Gson().fromJson(data, Array<LeaderboardItem>::class.java)
    }


    fun toMain(view: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}