package com.tasktinder.tinder.Matches

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.tasktinder.tinder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class MatchesActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mMatchesAdapter: RecyclerView.Adapter<*>? = null
    private var mMatchesLayoutManager: RecyclerView.LayoutManager? = null
    private var currentUserID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches)
        currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        mRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        mRecyclerView!!.isNestedScrollingEnabled = false
        mRecyclerView!!.setHasFixedSize(true)
        mMatchesLayoutManager = LinearLayoutManager(this@MatchesActivity)
        mRecyclerView!!.layoutManager = mMatchesLayoutManager
        mMatchesAdapter = MatchesAdapter(dataSetMatches, this@MatchesActivity)
        mRecyclerView!!.adapter = mMatchesAdapter
        userPendingId
        userMatchId
    }

    private val userPendingId: Unit
        private get() {
            val pendingDb = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID).child("connections").child("pending")
            pendingDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (pending in dataSnapshot.children) {
                            FetchPendingInformation(pending.key)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun FetchPendingInformation(key: String) {
        val userDb = FirebaseDatabase.getInstance().reference.child("Users").child(key)
        userDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userId = dataSnapshot.key
                    var name = ""
                    var profileImageUrl = ""
                    if (dataSnapshot.child("name").value != null) {
                        name = dataSnapshot.child("name").value.toString()
                    }
                    if (dataSnapshot.child("profileImageUrl").value != null) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                    }
                    val obj = MatchesObject(userId, name, profileImageUrl, "")
                    resultsPendings.add(obj)
                    mMatchesAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private val userMatchId: Unit
        private get() {
            val matchDb = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserID).child("connections").child("matches")
            matchDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (match in dataSnapshot.children) {
                            FetchMatchInformation(match.key, match.child("ChatId").value.toString())
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun FetchMatchInformation(key: String, chatId: String) {
        val userDb = FirebaseDatabase.getInstance().reference.child("Users").child(key)
        userDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userId = dataSnapshot.key
                    var name = ""
                    var profileImageUrl = ""
                    if (dataSnapshot.child("name").value != null) {
                        name = dataSnapshot.child("name").value.toString()
                    }
                    if (dataSnapshot.child("profileImageUrl").value != null) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                    }
                    val obj = MatchesObject(userId, name, profileImageUrl, chatId)
                    resultsMatches.add(obj)
                    mMatchesAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private val resultsMatches = ArrayList<MatchesObject>()
    private val dataSetMatches: List<MatchesObject>
        private get() = resultsMatches

    private val resultsPendings = ArrayList<MatchesObject>()
    private val dataSetPendings: List<MatchesObject>
        private get() = resultsPendings
}