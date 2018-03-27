package com.york.android.exomusicplayer.view.mymusic

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.york.android.exomusicplayer.R
import com.york.android.exomusicplayer.model.LibraryItem

/**
 * Created by York on 2018/3/27.
 */
class LibraryItemAdapter(val items: List<LibraryItem>, val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1) {
            val view = LayoutInflater.from(context).inflate(R.layout.libraryitem_recyclerview, parent, false)
            return LibraryItemHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.librarytitle_recyclerview, parent, false)
            return  LibraryTitleHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if(items[position].isItem) {
            (holder as LibraryItemHolder).bind(items[position])
        } else {
            (holder as LibraryTitleHolder).bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(items[position].isItem) {
            return 1
        } else {
            return 0
        }
    }

    inner class LibraryItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: LibraryItem) {
            val textViewName = itemView.findViewById<TextView>(R.id.textView_libraryItem_itemName)
            val textViewNumber = itemView.findViewById<TextView>(R.id.textView_libraryItem_number)

            textViewName.setText(item.name)
            textViewNumber.setText(item.songNumber.toString())
        }
    }

    inner class LibraryTitleHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: LibraryItem) {
            val textViewTitle = itemView.findViewById<TextView>(R.id.textView_librarytitle_title)

            textViewTitle.setText(item.name)
        }
    }
}