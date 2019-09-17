package com.example.a4_1_kt

import retrofit2.Call
import retrofit2.http.*

data class ResponseDTO(var result:String? = null)

interface RetrofitService{
    //post
    @FormUrlEncoded
    @POST("/test")
    fun postRequest(@Field("id") id: String,
                    @Field("pw") pw: String): Call<ResponseDTO>

    //post2
    @POST("/{path}")
    fun testRequest(@Path("path")path: String, @Body parameters: HashMap<String, Any>): Call<ResponseDTO>

    @GET("/{path}")
    fun testRequest2(@Path("path")path: String, @QueryMap parameters: HashMap<String, Any>): Call<ResponseDTO>

    @GET("/{path}")
    fun test(@Path("path")path: String, @QueryMap parameters: HashMap<String, Any>): Call<Void>
}