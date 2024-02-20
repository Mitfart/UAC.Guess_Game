package ru.zarichan.change_scene

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import kotlin.random.Random


enum class GameDifficulty {
    Easy,
    Normal,
    Hard
}

enum class GameTheme {
    System,
    Light,
    Dark,
}

class MainActivity : AppCompatActivity() {
    private lateinit var label: TextView
    private lateinit var startBtn: Button
    private lateinit var nameInput: EditText

    private lateinit var themeName: TextView

    private lateinit var sharedRef: SharedPreferences
    private lateinit var savedTheme: GameTheme
    private lateinit var savedDifficulty: GameDifficulty

    private var userName: String = ""
    private var theme: GameTheme = GameTheme.System
    private var difficulty: GameDifficulty = GameDifficulty.Normal


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        label = findViewById(R.id.mLabel)
        startBtn = findViewById(R.id.mBtn_start)
        nameInput = findViewById(R.id.mName)

        themeName = findViewById(R.id.mThemeName)

        sharedRef = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        savedTheme = GameTheme.values()[sharedRef.getInt("themeId", 0)]
        savedDifficulty = GameDifficulty.values()[sharedRef.getInt("difficultyId", 1)]

        nameInput.doOnTextChanged { text, _, _, _ -> setName(text.toString()) }
        nameInput.setText(sharedRef.getString("userName", ""))

        reset()

        val extras = intent.extras
        if (extras != null) {
            val number = extras.getInt("hiddenNum")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when (extras.getSerializable("state", GameState::class.java)) {
                    GameState.Win -> win(number)
                    GameState.Lose -> lose(number)
                    else -> reset()
                }

                difficulty = extras.getSerializable("difficulty", GameDifficulty::class.java)!!
            } else {
                when (extras.getSerializable("state")) {
                    GameState.Win -> win(number)
                    GameState.Lose -> lose(number)
                    else -> reset()
                }
                difficulty = extras.getSerializable("difficulty") as GameDifficulty
            }
            extras.clear()
        }

        registerForContextMenu(themeName)

        setTheme(savedTheme, true)
        difficulty = savedDifficulty

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

        return super.onOptionsItemSelected(item)
    }


    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (v == null || menu == null) {
            super.onCreateContextMenu(menu, v, menuInfo)
            return
        }

        when (v.id) {
            R.id.mTheme or R.id.mThemeName -> {
                for (gameTheme in GameTheme.values())
                    menu.add(0, gameTheme.ordinal, 0, gameTheme.toString())
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        setTheme(GameTheme.values()[item.itemId])
        return super.onContextItemSelected(item)
    }


    private fun setName(uName: String?) {
        userName = uName.toString()

        sharedRef
            .edit()
            .putString("userName", userName)
            .apply();
    }

    private fun setTheme(gameTheme: GameTheme, noRefresh: Boolean = false) {
        theme = gameTheme;
        themeName.text = theme.toString()

        when (gameTheme) {
            GameTheme.System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            GameTheme.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            GameTheme.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        sharedRef
            .edit()
            .putInt("themeId", theme.ordinal)
            .apply();

        if (noRefresh) return

        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        overridePendingTransition(0, 0);
        finish()
    }


    private fun reset() {
        label.text = getText(R.string.m_label)
        startBtn.text = getText(R.string.g_start_btn)
        nameInput.visibility = VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun lose(number: Int) {
        label.text = "$userName, ${getText(R.string.g_label_lose)} \n $number"
        startBtn.text = getText(R.string.g_restart_btn)
        nameInput.visibility = INVISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun win(number: Int) {
        label.text = "$userName, ${getText(R.string.g_label_win)} \n $number"
        startBtn.text = getText(R.string.g_restart_btn)
        nameInput.visibility = INVISIBLE
    }


    fun startGame(view: View) {
        val name = nameInput.text.toString()
        val i = Intent(this, GameActivity::class.java)

        i.putExtra("name", name.ifEmpty { "Guest" })
        i.putExtra("difficulty", difficulty)
        i.putExtra("hiddenNum", Random.nextInt(0, 100))
        when (difficulty) {
            GameDifficulty.Easy -> {}

            GameDifficulty.Normal -> {
                i.putExtra("attempts", 10)
            }

            GameDifficulty.Hard -> {
                i.putExtra("attempts", 10)
                i.putExtra("time", 30)
            }
        }

        startActivity(i)
    }


    fun openLeaderBoard(view: View) {
        sharedRef
            .edit()
            .putInt("difficultyId", difficulty.ordinal)
            .apply();

        val i = Intent(this, LeaderboardActivity::class.java)
        startActivity(i)
    }
}