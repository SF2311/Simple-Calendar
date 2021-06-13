package com.simplemobiletools.calendar.pro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.activities.SimpleActivity
import com.simplemobiletools.calendar.pro.dialogs.WebFeedEditDialog
import com.simplemobiletools.calendar.pro.models.WebCalendarFeed

class WebCalendarFeedAdapter(private val activity : SimpleActivity, private val webFeeds : Array<WebCalendarFeed>) : RecyclerView.Adapter<WebCalendarFeedAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textView : TextView
        init {
            textView = view.findViewById<TextView>(R.id.webfeed_edit_text)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.webfeed_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.setOnClickListener {
            WebFeedEditDialog(activity,webFeeds[position]){
                webFeeds[position] = it
                notifyDataSetChanged()
            }
        }
        viewHolder.textView.text = webFeeds[position].feedName
    }
    override fun getItemCount() = webFeeds.size

}
