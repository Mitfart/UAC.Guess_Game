package ru.zarichan.guess_game.adapters.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.zarichan.guess_game.GameDifficulty
import ru.zarichan.guess_game.R

class EazyLeaderboardAdapter(private val leaders: Array<LeaderboardItem>) :
    RecyclerView.Adapter<EazyLeaderboardAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.items__name)
        val attempts: TextView = itemView.findViewById(R.id.items__attemps)
        val time: TextView = itemView.findViewById(R.id.items__time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard__recycler_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val leader = leaders[position]
        holder.name.text = leader.name
        holder.attempts.text = if (leader.attempts != null) leader.attempts.toString() else ""
        holder.time.text = if (leader.time != null) leader.time.toString() else ""
    }

    override fun getItemCount() = leaders.size
}

class LeaderboardItem(
    val difficulty: GameDifficulty,
    var name: String,
    var attempts: Int? = null,
    var time: Float? = null
) {
}
