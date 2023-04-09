package com.yeocak.wordpuzzle.ui.screen.score

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yeocak.wordpuzzle.R
import com.yeocak.wordpuzzle.model.Score

class ScoreAdapter(private val dataSet: List<Score>) :
    RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderTextView: TextView
        val nameTextView: TextView
        val scoreTextView: TextView

        init {
            // Define click listener for the ViewHolder's View
            orderTextView = view.findViewById(R.id.orderTextView)
            nameTextView = view.findViewById(R.id.nameTextView)
            scoreTextView = view.findViewById(R.id.scoreTextView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_leaderboard, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val order: Int = position + 1
        viewHolder.orderTextView.text = "$order."
        viewHolder.nameTextView.text = dataSet[position].name
        viewHolder.scoreTextView.text = dataSet[position].score.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}