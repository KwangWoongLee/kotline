package com.example.a4_1_kt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment1.view.*
import kotlinx.android.synthetic.main.markerimage.view.*


class Fragment1 : Fragment(), OnMapReadyCallback {
    lateinit var adapter : MainAdapter
    lateinit var rlist: RecyclerView
    var db = FirebaseDatabase.getInstance()
    var ref = db.getReference()
    lateinit var me :Location
    var friendMarker :Marker? = null
    var myMarker :Marker? =null
    lateinit var mapView: MapView
    var itemList:ArrayList<item>? = arrayListOf()
    var map:GoogleMap? = null
    var isFirst = true
    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        MapsInitializer.initialize(this.context)

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView:View = inflater.inflate(R.layout.fragment1, container, false)
        mapView = rootView.mapview
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)


        return rootView
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    fun MapSetting(X:Double, Y:Double,PhotoUrl: String?,Id:String ){
        me = Location("me")
        me.setLatitude(X)
        me.setLongitude(Y)
        var other = Location("other")
        var distance:Float = 0.toFloat()


        VolleyService.testVolley(this.context) { testSuccess, response ->
            if (testSuccess) {
                var marker_root_view = LayoutInflater.from(this.context).inflate(R.layout.markerimage, null)
                var marker = marker_root_view.photo

                var othermarker_root_view = LayoutInflater.from(this.context).inflate(R.layout.othermarkerimage, null)
                var markershleter =othermarker_root_view.photo
                for (i in 0..30) {
                    other.setLatitude(response!!.get(i).x!!)
                    other.setLongitude(response.get(i).y!!)


                    if(i==1)
                        distance = me.distanceTo(other)
                    else{
                        if(distance<0.5){
                        }
                        if(distance>me.distanceTo(other)) {
                            distance = me.distanceTo(other)
                            map?.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        other.latitude,
                                        other.longitude
                                    )
                                ).title(response.get(i).name).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this.context!!, othermarker_root_view))))


                            Glide.with(activity).load(PhotoUrl).into(marker)


                            if(myMarker!=null)
                                myMarker!!.remove()
                            myMarker = map?.addMarker(MarkerOptions().position(LatLng(X, Y)).title("내위치").icon(
                                BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this.context!!, marker_root_view))))!!


                            if(isFirst){
                                map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(X,Y)))
                                isFirst= false
                                map?.animateCamera(CameraUpdateFactory.zoomTo(15F))
                            }
                            mapView.getMapAsync(this)
                        }

                }
                }


            }
        }

    }

    fun addFriend(X:Double,Y:Double,Name:String,PhotoUrl:String?){
        var marker_root_view = LayoutInflater.from(this.context).inflate(R.layout.markerimage, null)
        var marker = marker_root_view.photo
        Glide.with(activity).load(PhotoUrl).into(marker)
        if(friendMarker?.title.equals(Name))
            friendMarker!!.remove()
        friendMarker = map?.addMarker(MarkerOptions().position(LatLng(X,Y)).title(Name).icon(
            BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this.context!!, marker_root_view))))

        mapView.getMapAsync(this)

    }
    fun removeFriend(Name:String){
        if(friendMarker?.title.equals(Name))
            friendMarker!!.remove()
        mapView.getMapAsync(this)

    }

    fun addMe(PhotoUrl: String?){

    }

    private fun createDrawableFromView(context: Context, view:View ): Bitmap? {

        var displayMetrics = DisplayMetrics();
        activity!!.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        var bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        var canvas = Canvas(bitmap);
        view.draw(canvas);

        return bitmap
    }

}