package com.example.a4_1_kt

import com.google.firebase.database.Exclude
data class FriendItem(var name:String ="", var id:String ="", var x:Double =0.0, var y:Double =0.0,var flag:String="",var key:String="",var photoUrl:String="",var email:String="")
{

    data class FriendItem(var name:String="",var photoUrl: String="",var flag: String=""){

    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "x" to x,
            "y" to y,
            "flag" to flag,
            "key" to key,
            "photoUrl" to photoUrl
        )
    }
}