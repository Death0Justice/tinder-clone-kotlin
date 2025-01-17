package com.tasktinder.tinder.Chat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.tasktinder.tinder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mChatAdapter: RecyclerView.Adapter<*>? = null
    private var mChatLayoutManager: RecyclerView.LayoutManager? = null
    private var mSendEditText: EditText? = null
    private var mSendButton: Button? = null
    private var currentUserID: String? = null
    private var matchId: String? = null
    private var chatId: String? = null
    var mDatabaseUser: DatabaseReference? = null
    var mDatabaseChat: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        matchId = intent.extras!!.getString("matchId")
        chatId = intent.extras!!.getString("chatId")
        currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        mDatabaseUser = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID).child("connections").child("matches").child(matchId).child("ChatId")
        mDatabaseChat = FirebaseDatabase.getInstance().reference.child("Chat").child(chatId)
        mRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        mRecyclerView!!.isNestedScrollingEnabled = false
        mRecyclerView!!.setHasFixedSize(false)
        mChatLayoutManager = LinearLayoutManager(this@ChatActivity)
        mRecyclerView!!.layoutManager = mChatLayoutManager
        mChatAdapter = ChatAdapter(dataSetChat, this@ChatActivity)
        mRecyclerView!!.adapter = mChatAdapter
        mSendEditText = findViewById(R.id.message)
        mSendButton = findViewById(R.id.send)
        mSendButton?.setOnClickListener(View.OnClickListener { sendMessage() })
        chatMessages
    }

    private fun sendMessage() {
        val sendMessageText = mSendEditText!!.text.toString()
//        if (!sendMessageText.isEmpty()) {
//            val newMessageDb = mDatabaseChat!!.push()
//            val newMessage = mutableMapOf<Any?, Any?>("createdByUser" to currentUserID, "text" to sendMessageText)
//            newMessageDb.setValue(newMessage)
//        }
        mSendEditText.run {
            if (!sendMessageText.isEmpty()) {
                val newMessageDb = mDatabaseChat!!.push()
                val newMessage = mutableMapOf<Any?, Any?>("createdByUser" to currentUserID, "text" to sendMessageText)
                newMessageDb.setValue(newMessage)
            }
            mSendEditText!!.setText("")
        }
    }

    private val chatMessages: Unit
        private get() {
            mDatabaseChat!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.exists()) {
                        var message: String? = null
                        var createdByUser: String? = null
                        if (dataSnapshot.child("text").value != null) {
                            message = dataSnapshot.child("text").value.toString()
                        }
                        if (dataSnapshot.child("createdByUser").value != null) {
                            createdByUser = dataSnapshot.child("createdByUser").value.toString()
                        }
                        if (message != null && createdByUser != null) {
                            var currentUserBoolean = false
                            if (createdByUser == currentUserID) {
                                currentUserBoolean = true
                            }
                            val newMessage = ChatObject(message, currentUserBoolean)
                            resultsChat.add(newMessage)
                            mChatAdapter!!.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private val resultsChat = ArrayList<ChatObject>()
    private val dataSetChat: List<ChatObject>
        private get() = resultsChat
}

//private fun EditText?.setText() {
//
//}


private operator fun <K, V> MutableMap<K, V>.set(v: String, value: String?) {

}
