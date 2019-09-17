package com.example.a4_1_kt

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.item_main.view.*

class MainAdapter(val id: String) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
    lateinit var context:Context
    var items: ArrayList<MainData>? = arrayListOf()
    var item2: MainData? =null
    var db = FirebaseDatabase.getInstance()
    var ref = db.getReference()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MainViewHolder {
        context = parent.context

        return MainViewHolder(parent)
    }

    override fun getItemCount(): Int = items!!.size

    override fun onBindViewHolder(holer: MainViewHolder, position: Int) {
        items!![position].let { item ->
            with(holer) {
                name.text = item.name
                Glide.with(context).load(item.photoUrl).into(photo)
                if(item.flag.equals("1")){
                    state.setImageResource(R.drawable.green)
                }
                else{
                    state.setImageResource(R.drawable.red)
                }
                btn.setOnClickListener {
//                    Toast.makeText(context,"호출"+item.name,Toast.LENGTH_LONG).show()
                    ref.child("user").child(id).child("call").child(item.id).child("name").setValue(item.name)
                    ref.child("user").child(id).child("call").child(item.id).child("id").setValue(item.id)
                    ref.child("user").child(id).child("call").child(item.id).child("state").setValue("1")
//                    ref.child("user").child(item.id).child("call").child(id).addListenerForSingleValueEvent(callListener)
                }
            }
        }
    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(

        LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false)) {
        val btn = itemView.btn_call
        val name = itemView.name
        val photo = itemView.photo
        val state = itemView.state
    }

    fun setData(list: ArrayList<MainData>){
        this.items = list
    }

    fun setItem(m :MainData){
        this.item2 = m
    }
    val callListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

        }
        override fun onCancelled(databaseError: DatabaseError) {

        }
    }
}
