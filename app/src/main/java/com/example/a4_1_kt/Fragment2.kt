package com.example.a4_1_kt

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment2.view.*


@SuppressLint("ValidFragment")
class Fragment2 @SuppressLint("ValidFragment") constructor(var id: String) : Fragment() {
    lateinit var adapter : MainAdapter
    lateinit var list: ArrayList<MainData>
    lateinit var rlist:RecyclerView
    var db = FirebaseDatabase.getInstance()
    var ref = db.getReference()
    var friendsRef = ref.child("user").child(id).child("friends")
    var sizeRef = ref.child("user").child(id).child("friends").child("size")
    var size = 0
    var flag = ""


    val sizeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            size = dataSnapshot.getValue(Int::class.java)!!
            if(size >=1) {
                list = arrayListOf()
                for (i in 1..size) {

                    val arrayListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            list.add(dataSnapshot.getValue(MainData::class.java)!!)
                            val flagListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    flag = dataSnapshot.getValue(String::class.java)!!
                                    if(flag.equals("1")){
                                        list[i-1].flag="1"
                                        adapter.setData(list)
                                        rlist.adapter = adapter
                                    }
                                    else{
                                        list[i-1].flag="0"
                                        adapter.setData(list)
                                        rlist.adapter = adapter
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            }
                            friendsRef.child(i.toString()).child("flag").addValueEventListener(flagListener)
                            adapter.setData(list)
                            rlist.adapter = adapter
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    }


                    friendsRef.child(i.toString()).addListenerForSingleValueEvent(arrayListener)
                }
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment2, container, false)
        rlist = rootView.recycler_view
        sizeRef.addValueEventListener(sizeListener)
        adapter = MainAdapter(id)
        adapter.setData(list)
        rlist.adapter = adapter

        rlist.layoutManager = LinearLayoutManager(context)


        return rootView
    }


    init {
        this.list = arrayListOf()
        this.db = FirebaseDatabase.getInstance()
        this.ref = db.getReference()
    }

}