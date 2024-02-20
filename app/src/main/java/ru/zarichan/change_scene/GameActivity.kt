package ru.zarichan.change_scene

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ru.zarichan.change_scene.adapters.leaderboard.LeaderboardItem

enum class GameState {
    None,
    Win,
    Lose
}

enum class GuessState {
    None,
    Less,
    More
}

class GameActivity : AppCompatActivity() {
    private lateinit var label: TextView
    private lateinit var inputField: EditText
    private lateinit var attemptsView: TextView
    private lateinit var timerView: TextView
    private lateinit var userNameView: TextView

    private var userName: String = ""
    private var hiddenNum: Int = -1
    private lateinit var difficulty: GameDifficulty

    private var maxAttempts: Int = 5
    private var attempts: Int = 0

    private var time: Long = 0
    private var passedTime: Long = 0
    private lateinit var timer: CountDownTimer
    private lateinit var sharedRef: SharedPreferences
    private lateinit var leaderboardRef: SharedPreferences

    private var guessState = GuessState.None


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        label = findViewById(R.id.gLabel)
        inputField = findViewById(R.id.gInput)
        attemptsView = findViewById(R.id.gAttempts)
        timerView = findViewById(R.id.gTimer)
        userNameView = findViewById(R.id.gUserName)

        sharedRef = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        leaderboardRef = getSharedPreferences("LEADERBOARD", Context.MODE_PRIVATE)

        setName(sharedRef.getString("userName", ""))

        val extras = intent.extras
        if (extras != null) {
            hiddenNum = extras.getInt("hiddenNum", 50)

            difficulty =
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    extras.getSerializable("difficulty", GameDifficulty::class.java)!!
                else
                    extras.getSerializable("difficulty") as GameDifficulty)


            if (difficulty == GameDifficulty.Normal
                || difficulty == GameDifficulty.Hard
            ) {
                maxAttempts = extras.getInt("attempts")
                attempts = savedInstanceState?.getInt("attempts") ?: 0
                attemptsView.visibility = View.VISIBLE
            } else {
                attemptsView.visibility = View.GONE
            }


            if (difficulty == GameDifficulty.Hard) {
                timerView.visibility = View.VISIBLE
                time = extras.getInt("time").toLong() * 1000
                passedTime = savedInstanceState?.getLong("passedTime") ?: 0

                timer = object : CountDownTimer(
                    time - passedTime,
                    1000
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        val remainingTime = (millisUntilFinished * .001).toLong()
                        passedTime = time - millisUntilFinished
                        timerView.text = remainingTime.toString()
                    }

                    override fun onFinish() {
                        lose()
                        passedTime = 0
                    }
                }
                timer.start()
            } else {
                timerView.visibility = View.GONE
            }
        }

        updateAttempts()

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                setGuessState(
                    savedInstanceState.getSerializable(
                        "guessState",
                        GuessState::class.java
                    )!!
                )
            } else {
                setGuessState(
                    savedInstanceState.getSerializable(
                        "guessState"
                    ) as GuessState
                )
            }
        }

        userNameView.text = userName
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("attempts", attempts)
        outState.putLong("passedTime", passedTime)
        outState.putSerializable("guessState", guessState)
    }


    private fun setName(uName: String?) {
        userName = uName.toString()
        userNameView.text = userName
    }


    fun guess(view: View) {
        val input: String = inputField.text.toString()
        inputField.text.clear()

        if (input.isNotEmpty()) {
            val inputNum: Int = input.toInt()
            guessState = GuessState.None

            when {
                inputNum > hiddenNum -> less()
                inputNum < hiddenNum -> more()
                else -> win()
            }
        }
    }

    private fun less() {
        setGuessState(GuessState.Less)
        useAttempt()
    }

    private fun more() {
        setGuessState(GuessState.More)
        useAttempt()
    }

    private fun setGuessState(state: GuessState) {
        guessState = state
        when (state) {
            GuessState.None -> return
            GuessState.Less -> setLabel(R.string.g_label_less)
            GuessState.More -> setLabel(R.string.g_label_more)
        }
    }

    private fun win() {
        val leaderboardItem = LeaderboardItem(
            difficulty,
            userName,
            if (difficulty == GameDifficulty.Normal || difficulty == GameDifficulty.Hard) attempts else null,
            if (difficulty == GameDifficulty.Hard) (passedTime * .001).toFloat() else null
        )

        val leaders = getLeadersList(difficulty).plus(leaderboardItem)

        val key = when (difficulty) {
            GameDifficulty.Hard -> "leaders__hard"
            GameDifficulty.Normal -> "leaders__normal"
            GameDifficulty.Easy -> "leaders__easy"
        }

        val leaderboardEditor = leaderboardRef.edit()
        leaderboardEditor.putString(key, Gson().toJson(leaders))
        leaderboardEditor.apply()
        toMain(GameState.Win)
    }

    private fun lose() = toMain(GameState.Lose)
    fun exit(view: View) = toMain(GameState.None)


    private fun useAttempt() {
        attempts++

        if (difficulty != GameDifficulty.Easy) {
            updateAttempts()
            checkLose()
        }
    }

    private fun updateAttempts() {
        attemptsView.text = (maxAttempts - attempts).toString()
    }

    private fun checkLose() {
        if (attempts >= maxAttempts) lose()
    }


    private fun setLabel(resId: Int) {
        label.text = getText(resId)
    }


    private fun toMain(state: GameState) {
        if (difficulty == GameDifficulty.Hard)
            timer.cancel()

        val i = Intent(this, MainActivity::class.java)
        i.putExtra("name", userName)
        i.putExtra("state", state)
        i.putExtra("hiddenNum", hiddenNum)
        i.putExtra("attempts", attempts)
        i.putExtra("difficulty", difficulty)
        startActivity(i)
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
}