package com.example.a4_1_kt

import android.content.Context
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.lang.Double
import java.lang.reflect.Method

object VolleyService {
    val testUrl = "https://openapi.gg.go.kr/Heatwaverestarere?Key=74412de55b14412e9a8b886fdcc6d406&SIGUN_NM=%EC%84%B1%EB%82%A8%EC%8B%9C&Type=json&RESTARER_TYPE_DIV_NM=%EB%85%B8%EC%9D%B8%EC%8B%9C%EC%84%A4"
    lateinit var item: JSONObject
    lateinit var itemName: String
    lateinit var itemAddr: String
    lateinit var itemX: String
    lateinit var itemY: String
    var itemList:ArrayList<item> = arrayListOf()



    fun testVolley(context: Context?, success: (Boolean,ArrayList<item>?) -> Unit) {


        val testRequest = object : StringRequest(
            Method.GET, testUrl , Listener { response ->
            println("서버 Response 수신: $response")



            val JO= JSONObject(response)
            val JR = JO.getJSONArray("Heatwaverestarere")
            val JRR = JR.getJSONObject(1)
            val row = JRR.getJSONArray("row")

            for(i in 0..30){
                item =row.getJSONObject(i)
                itemX = item.getString("REFINE_WGS84_LAT")
                itemY = item.getString("REFINE_WGS84_LOGT")
                itemName = item.getString("RESTARER_FACLT_NM")
                itemAddr = item.getString("REFINE_ROADNM_ADDR")
                itemList.add(item(itemName, itemAddr, Double.parseDouble(itemX), Double.parseDouble(itemY)))

            }



            success(true, itemList)
        }, Response.ErrorListener { error ->
            success(false,null)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray? {
                return null;
            }


            /* getBodyContextType에서는 요청에 포함할 데이터 형식을 지정한다.
             * getBody에서는 요청에 JSON이나 String이 아닌 ByteArray가 필요하므로, 타입을 변경한다. */
        }

        Volley.newRequestQueue(context).add(testRequest)

    }
}