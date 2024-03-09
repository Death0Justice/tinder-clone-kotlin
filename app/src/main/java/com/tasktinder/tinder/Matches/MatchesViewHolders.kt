package com.tasktinder.tinder.Matches

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.tasktinder.tinder.Chat.ChatActivity
import com.tasktinder.tinder.R

/**
 * Created by manel on 10/31/2017.
 */
class MatchesViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var mMatchId: TextView
    var mChatId: TextView
    var mMatchName: TextView
    var mMatchImage: ImageView
    override fun onClick(view: View) {
        val intent = Intent(view.context, ChatActivity::class.java)
        val b = Bundle()
        b.putString("matchId", mMatchId.text.toString())
        b.putString("chatId", mChatId.text.toString())
        intent.putExtras(b)
        view.context.startActivity(intent)
    }

    init {
        itemView.setOnClickListener(this)
        mMatchId = itemView.findViewById<View>(R.id.Matchid) as TextView
        mChatId = itemView.findViewById<View>(R.id.Chatid) as TextView
        mMatchName = itemView.findViewById<View>(R.id.MatchName) as TextView
        mMatchImage = itemView.findViewById<View>(R.id.MatchImage) as ImageView
    }
}