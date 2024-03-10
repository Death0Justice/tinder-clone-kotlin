package com.tasktinder.tinder

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import com.tasktinder.tinder.Cards.arrayAdapter
import com.tasktinder.tinder.Cards.cards
import com.tasktinder.tinder.Matches.MatchesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.lorentzos.flingswipe.SwipeFlingAdapterView.onFlingListener
import com.tasktinder.tinder.Matches.MatchesObject
import java.util.*

class MainActivity : AppCompatActivity() {
    private val cards_data: Array<cards>? = null
    private var arrayAdapter: arrayAdapter? = null
    private val i = 0
    private var mAuth: FirebaseAuth? = null
    private var currentUId: String? = null
    private var usersDb: DatabaseReference? = null
    var listView: ListView? = null
    var rowItems: MutableList<cards>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usersDb = FirebaseDatabase.getInstance().reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        currentUId = mAuth?.getCurrentUser()!!.uid
        checkUserAcceptance()
        rowItems = ArrayList()
        arrayAdapter = arrayAdapter(this, R.layout.item, rowItems)
        val flingContainer = findViewById<View>(R.id.frame) as SwipeFlingAdapterView
        flingContainer.adapter = arrayAdapter
        flingContainer.setFlingListener(object : onFlingListener {
            override fun removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!")
                rowItems?.removeAt(0)
                arrayAdapter!!.notifyDataSetChanged()
            }

            override fun onLeftCardExit(dataObject: Any) {
                val obj = dataObject as cards
                val userId = obj.userId
                usersDb?.child(userId)?.child("connections")?.child("nope")?.child(currentUId)?.setValue(true)
                Toast.makeText(this@MainActivity, "Nope!", Toast.LENGTH_SHORT).show()
            }

            override fun onRightCardExit(dataObject: Any) {
                val obj = dataObject as cards
                val userId = obj.userId
                usersDb?.child(userId)?.child("connections")?.child("yeps")?.child(currentUId)?.setValue(true)
                usersDb?.child(currentUId)?.child("connections")?.child("pending")?.child(userId)?.setValue(true)
                isConnectionMatch(userId)
                Toast.makeText(this@MainActivity, "Yep!", Toast.LENGTH_SHORT).show()
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {}
            override fun onScroll(scrollProgressPercent: Float) {}
        })


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener { itemPosition, dataObject ->
//            Toast.makeText(this@MainActivity, "Item Clicked", Toast.LENGTH_SHORT).show()
            val obj = dataObject as cards
        }
    }

    private fun isConnectionMatch(userId: String) {
        val currentUserConnectionsDb = usersDb!!.child(currentUId).child("connections").child("yeps").child(userId)
        currentUserConnectionsDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(this@MainActivity, "new Connection", Toast.LENGTH_LONG).show()
                    val key = FirebaseDatabase.getInstance().reference.child("Chat").push().key
                    // remove the pending swipe if swiped partner swiped back
                    usersDb!!.child(dataSnapshot.key).child("connections").child("pending").child(currentUId).removeValue()
                    usersDb!!.child(currentUId).child("connections").child("pending").child(dataSnapshot.key).removeValue()
                    usersDb!!.child(dataSnapshot.key).child("connections").child("matches").child(currentUId).child("ChatId").setValue(key)
                    usersDb!!.child(currentUId).child("connections").child("matches").child(dataSnapshot.key).child("ChatId").setValue(key)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private var userAcceptance: String? = null
    private var oppositeUserAcceptance: String? = null
    fun checkUserAcceptance() {
        val user = FirebaseAuth.getInstance().currentUser
        val userDb = usersDb!!.child(user!!.uid)
        userDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("acceptance").value != null) {
                        userAcceptance = dataSnapshot.child("acceptance").value.toString()
                        when (userAcceptance) {
                            "Accept" -> oppositeUserAcceptance = "Provide"
                            "Provide" -> oppositeUserAcceptance = "Accept"
                        }
                        pendingUser
                        oppositeAcceptanceUser
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    val oppositeAcceptanceUser: Unit
        get() {
            usersDb!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.child("acceptance").value != null) {
                        if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && dataSnapshot.child("acceptance").value.toString() == oppositeUserAcceptance) {
                            var profileImageUrl = "default"
                            if (dataSnapshot.child("profileImageUrl").value != "default") {
                                profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                            }
                            val item = cards(dataSnapshot.key, dataSnapshot.child("name").value.toString(), profileImageUrl)
                            rowItems!!.add(item)
                            arrayAdapter!!.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }


    private val pendingUser: Unit
        private get() {
            val pendingDb = FirebaseDatabase.getInstance().reference.child("Users").child(currentUId).child("connections").child("pending")
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
                    var name = ""
                    var profileImageUrl = ""
                    if (dataSnapshot.child("name").value != null) {
                        name = dataSnapshot.child("name").value.toString()
                    }
                    if (dataSnapshot.child("profileImageUrl").value != null) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                    }
                    val item = cards(dataSnapshot.key, name, profileImageUrl)
                    rowItems!!.add(0, item)
                    arrayAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun logoutUser(view: View?) {
        mAuth!!.signOut()
        val intent = Intent(this@MainActivity, ChooseLoginRegistrationActivity::class.java)
        startActivity(intent)
        finish()
        return
    }

    fun goToSettings(view: View?) {
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(intent)
        return
    }

    fun goToMatches(view: View?) {
        val intent = Intent(this@MainActivity, MatchesActivity::class.java)
        startActivity(intent)
        return
    }
}