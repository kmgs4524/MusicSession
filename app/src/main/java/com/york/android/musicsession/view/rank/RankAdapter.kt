package com.york.android.musicsession.view.rank

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.york.android.musicsession.R
import com.york.android.musicsession.model.Rank

/**
 * Created by York on 2018/3/27.
 */
class RankAdapter(val items: List<Rank>, val context: Context): RecyclerView.Adapter<RankAdapter.RankItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RankItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rankitem_recyclerview, parent, false)
        return RankItemHolder(view)
    }

    override fun onBindViewHolder(holder: RankItemHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class RankItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(rank: Rank) {
            val textViewTitle = itemView.findViewById<TextView>(R.id.textView_rank_title)
            val textViewSubtitle = itemView.findViewById<TextView>(R.id.textView_rank_subtitle)
            val textViewTop = itemView.findViewById<TextView>(R.id.textView_rank_top)
            val textViewSecond = itemView.findViewById<TextView>(R.id.textView_rank_second)
            val textViewThird = itemView.findViewById<TextView>(R.id.textView_rank_third)

            textViewTitle.setText(rank.title)
            textViewSubtitle.setText(rank.subtitle)
            textViewTop.setText(rank.ranking[0])
            textViewSecond.setText(rank.ranking[1])
            textViewThird.setText(rank.ranking[2])
        }
    }
}