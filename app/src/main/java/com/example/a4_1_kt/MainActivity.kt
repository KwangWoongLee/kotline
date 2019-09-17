package com.example.a4_1_kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog

import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), SensorEventListener {


    //firebase database
    var db = FirebaseDatabase.getInstance()
    var ref = db.getReference()
    var location = ref.child("user").child("id").child("location")
    var size:Int = 0
    var FriendArray:ArrayList<MainData> = arrayListOf()
    var UserArray :ArrayList<FriendItem> = arrayListOf()
//firebase auth
lateinit var firebaseAuth: FirebaseAuth

    var fragment1 :Fragment1 = Fragment1()
    var server: RetrofitService? = null //레트로핏
    private var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    lateinit var progressDialog: ProgressDialog
//
    lateinit var name:String
    lateinit var id:String
    lateinit var photoUrl:String
    lateinit var email:String
    //

    // 위치정보가져오기
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 1000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10

    //    쉐이크 센서사용
    lateinit var mSensorManager: SensorManager
    lateinit var mAccelerometer: Sensor
    private var mShakeTime: Long = 0
    private val SHAKE_SKIP_TIME = 500
    private val SHAKE_THRESHOLD_GRAVITY = 2.7f
    internal var mShakeCount = 0

    @SuppressLint("ObsoleteSdkInt", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()
        FriendArray = ArrayList<MainData>()

        mLocationRequest = LocationRequest()
        photoUrl = intent.getStringExtra("photoUrl")
        name = intent.getStringExtra("name")
        id = intent.getStringExtra("id")
        email = intent.getStringExtra("email")
        ref.child("request").child(id).child("none").child("state").setValue("none")
        ref.child("response").child(id).child("none").child("state").setValue("none")

        ref.child("user").child(id).child("call").addValueEventListener(callListener)
        ref.child("user").child(id).child("name").setValue(name)
        ref.child("user").child(id).child("flag").setValue("1")
        ref.child("user").child(id).child("photoUrl").setValue(photoUrl)
        ref.child("user").child(id).child("email").setValue(email)
        ref.child("user").child(id).child("friends").child("size").addListenerForSingleValueEvent(sizeListener)
//        postFirebaseDB()

        viewPager = findViewById(R.id.viewpager) as ViewPager
        tabLayout = findViewById(R.id.pager_title_strip) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
        setupViewPager(viewPager!!)

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout!!.setOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))

        progressDialog = ProgressDialog(this)

        ref.child("response").child(id).addValueEventListener(requestListener)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }

        //
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
//firebase



//        sendSMS("01058529537","긴급호출! 지금 이곳에 구급차를 불러주세요.")




//        //레트로핏
//        var retrofit = Retrofit.Builder()
//            .baseUrl("http://192.168.43.161:3000")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        server = retrofit.create(RetrofitService::class.java)
//        onClick_btn()
//        //
    }


    //
    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()


    }
    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    public fun onLocationChanged(location: Location) {
        ref.child("user").child(id).child("location").child("x").setValue(location.latitude)
        ref.child("user").child(id).child("location").child("y").setValue(location.longitude)
        mLastLocation = location
        fragment1.MapSetting(mLastLocation.latitude,mLastLocation.longitude,photoUrl,id)

    }

    fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // Show the permission request
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }
    //
    private fun sendSMS(phoneNumber:String, message:String){
        var SENT:String = "SMS_SENT"
        var DELIVERED:String = "SMS_DELIVERD"

        var intent:Intent = Intent(Intent.ACTION_PICK)

        var sentPI:PendingIntent = PendingIntent.getBroadcast(this,0, Intent(SENT),0)
        var deliveredPI:PendingIntent = PendingIntent.getBroadcast(this,0,Intent(DELIVERED),0)


        var sms:SmsManager = SmsManager.getDefault()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Ask for permision
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                1
            );
        }

        sms.sendTextMessage(phoneNumber,null,message,sentPI,deliveredPI)
    }

    //
//    //레트로핏
//    fun onClick_btn() {
//
//        btn_test2.setOnClickListener {
//
//            var params: HashMap<String, Any> = HashMap<String, Any>()
//            params.put("id", "sunpil")
//            params.put("pw", 123123)
//
//            server?.testRequest2("test", params)?.enqueue(object : Callback<ResponseDTO> {
//                override fun onFailure(call: Call<ResponseDTO>?, t: Throwable?) {
//                    Toast.makeText(applicationContext,""+t,Toast.LENGTH_LONG).show()
//
//                }
//
//                override fun onResponse(call: Call<ResponseDTO>?, response: Response<ResponseDTO>?) {
//                    var res: ResponseDTO? = response?.body()
//                    Toast.makeText(applicationContext,""+res?.result,Toast.LENGTH_LONG).show()
//
//                }
//            })
//        }
//
//    }
    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }




    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(
            this,
            mAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)


    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val axisX = event.values[0]
            val axisY = event.values[1]
            val axisZ = event.values[2]

            val gravityX = axisX / SensorManager.GRAVITY_EARTH
            val gravityY = axisY / SensorManager.GRAVITY_EARTH
            val gravityZ = axisZ / SensorManager.GRAVITY_EARTH

            val f = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ
            val squaredD = Math.sqrt(f.toDouble())
            val gForce = squaredD.toFloat()
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val currentTime = System.currentTimeMillis()
                if (mShakeTime + SHAKE_SKIP_TIME > currentTime) {
                    return
                }
                mShakeTime = currentTime
                mShakeCount++
                Toast.makeText(applicationContext, "Shake 발생$mShakeCount", Toast.LENGTH_LONG).show()

            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }



//액션바에 메뉴추가
override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    val actionBar = supportActionBar
    if(actionBar != null) actionBar.setDisplayShowTitleEnabled(false)

    return true
}
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_search -> {
//                progressDialog.setMessage("로그아웃 중입니다...")
//                progressDialog.show()
                firebaseAuth.signOut()
                ref.child("user").child(id).child("logout").setValue("0")



                finish()
                return super.onOptionsItemSelected(item)
            }
            R.id.action_share -> {
                //공유 버튼 눌렀을 때
//                size++
//                ref.child("user").child(id).child("friends").child("size").setValue(size)
//                ref.child("user").child(id).child("friends").child(""+(size)).child("id").setValue(id+1)
//                ref.child("user").child(id).child("friends").child(""+(size)).child("x").setValue(mLastLocation.latitude)
//                ref.child("user").child(id).child("friends").child(""+(size)).child("y").setValue(mLastLocation.longitude)
//                ref.child("user").child(id).child("friends").child(""+(size)).child("name").setValue("엄다혜")
//                ref.child("user").child(id).child("friends").child(""+(size)).child("flag").setValue("0")
//                ref.child("user").child(id).child("friends").child(""+(size)).child("photoUrl").setValue(photoUrl)
//                ref.child("user").child(id).child("friends").child(""+(size)).child("key").setValue(""+(size))
//                ref.child("user").child(id).child("friends").child("size").addListenerForSingleValueEvent(sizeListener)
//
                val builder = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.plus_dialog2, null)
                val dialogText = dialogView.findViewById<EditText>(R.id.email)

                builder.setView(dialogView)
                    .setPositiveButton("확인") { dialogInterface, i ->
                        val reqsizeListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    var size = dataSnapshot.childrenCount

                                val getIdListener = object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        val userListener = object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                dataSnapshot.children.forEach {
                                                     val x = it.getValue(FriendItem::class.java)
                                                    if (x != null) {
                                                        if(x.email.equals(dialogText.text.toString())){
                                                            ref.child("request").child(id).child(x.id).child("state").setValue("0")
                                                            ref.child("request").child(id).child(x.id).child("name").setValue(x.name)
                                                            ref.child("request").child(id).child(x.id).child("id").setValue(x.id)
                                                            ref.child("response").child(x.id).child(id).child("state").setValue("0")
                                                            ref.child("response").child(x.id).child(id).child("name").setValue(name)
                                                            ref.child("response").child(x.id).child(id).child("id").setValue(id)

                                                        }

                                                    }
                                                }

                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {

                                            }
                                        }
                                        ref.child("user").addListenerForSingleValueEvent(userListener)

                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {

                                    }
                                }
                                ref.child("request").child(id).addListenerForSingleValueEvent(getIdListener)

                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        }

                        ref.child("request").child(id).addListenerForSingleValueEvent(reqsizeListener)
                    }
                    .setNegativeButton("취소") { dialogInterface, i ->
                        /* 취소일 때 아무 액션이 없으므로 빈칸 */
                    }
                    .show()



                return super.onOptionsItemSelected(item)
            }


            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(fragment1, "쉼터찾기")
        adapter.addFragment(Fragment2(id), "내친구보기")
        adapter.addFragment(Fragment3(), "?")
        viewPager.adapter = adapter
    }

//firebase Listener
//val getId1Listener = object : ValueEventListener {
//    override fun onDataChange(dataSnapshot: DataSnapshot) {
//        dataSnapshot.children.forEach {
//            val x = it.getValue(FriendItem::class.java)
//            if (x != null) {
//                if(x.email == dialogText.text.toString()){
//                    Toast.makeText(applicationContext,""+ x.email ,Toast.LENGTH_LONG).show()
//                }
//            }
//
//
//        }
//
//    }
//    override fun onCancelled(databaseError: DatabaseError) {
//
//    }
//}

    val requestListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            dataSnapshot.children.forEach {
                var x=it.getValue(requestItem::class.java)
                if (x != null && !x.state.equals("none") && !x.state.equals("1")) {

                    val builder = AlertDialog.Builder(this@MainActivity)
                    val dialogView = layoutInflater.inflate(R.layout.plus_dialog, null)
                    val dialogName = dialogView.findViewById<TextView>(R.id.name)
                    dialogName.setText(x.name)
                    builder.setView(dialogView)
                        .setPositiveButton("확인") { dialogInterface, i ->

                            val okListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    ref.child("request").child(id).child(x.id).child("state").setValue("1")
                                }
                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            }
                            ref.child("response").child(x.id).child(id).child("state").addValueEventListener(okListener)

                            ref.child("response").child(x.id).child(id).child("state").setValue("1")
                        }
                        .setNegativeButton("취소") { dialogInterface, i ->
                            /* 취소일 때 아무 액션이 없으므로 빈칸 */
                        }
                        .show()
                         }

                }
            }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    val callListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            dataSnapshot.children.forEach {
                val x= it.getValue(callItem::class.java)
                if (x != null) {
                    if(x.state.equals("1")) {
                        val builder = AlertDialog.Builder(this@MainActivity)
                        val dialogView = layoutInflater.inflate(R.layout.plus_dialog3, null)
                        val dialogText = dialogView.findViewById<TextView>(R.id.callName)
                        dialogText.setText(x.name)
                        builder.setView(dialogView)
                            .setPositiveButton("확인") { dialogInterface, i ->
                                ref.child("user").child(id).child("call").child(x.id).child("state").setValue("0")

                            }
                            .show()
            //                    Toast.makeText(applicationContext,"" +it.key ,Toast.LENGTH_LONG).show()
                    }
                }


        }
        }
        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

val sizeListener = object : ValueEventListener {
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        size = dataSnapshot.getValue(Int::class.java)!!
        if(size>=1) {
            for (i in 1..size)
                ref.child("user").child(id).child("friends").child(i.toString()).addListenerForSingleValueEvent(
                    fchildListener
                )
        }
    }
    override fun onCancelled(databaseError: DatabaseError) {

    }
}


val fchildListener = object :ValueEventListener{
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        // Get Post object and use the values to update the UI
        val friendItem = dataSnapshot.getValue(FriendItem::class.java)!!
        val flagListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val flag= dataSnapshot.getValue()
                ref.child("user").child(id).child("friends").child(friendItem.key).child("flag").setValue(flag)
                if(flag.toString().equals("1")) {
                    fragment1.addFriend(friendItem.x, friendItem.y, friendItem.name,friendItem.photoUrl)
                    FriendArray.add(MainData(friendItem.name,friendItem.photoUrl,friendItem.flag.toString()))
                    }
                else if(flag.toString().equals("0")){
                    fragment1.removeFriend(friendItem.name)
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        val xListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val x= dataSnapshot.getValue()
                ref.child("user").child(id).child("friends").child(friendItem.key).child("x").setValue(x)
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        val yListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val y= dataSnapshot.getValue()
                ref.child("user").child(id).child("friends").child(friendItem.key).child("y").setValue(y)

            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
//        val photoListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val photoUrl= dataSnapshot.getValue()
//                ref.child("user").child(id).child("friends").child(friendItem.key).child("photoUrl").setValue(photoUrl)
//
//            }
//            override fun onCancelled(databaseError: DatabaseError) {
//
//            }
//        }
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    fragment1.addFriend(friendItem.x,friendItem.y,friendItem.id,friendItem.photoUrl)

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        ref.child("user").child(friendItem.id).child("flag").addValueEventListener(flagListener)
        ref.child("user").child(friendItem.id).child("location").child("x").addValueEventListener(xListener)
        ref.child("user").child(friendItem.id).child("location").child("y").addValueEventListener(yListener)
//        ref.child("user").child(friendItem.id).child("photoUrl").addValueEventListener(photoListener)
        ref.child("user").child(friendItem.id).child("location").addChildEventListener(childEventListener)


    }

    override fun onCancelled(databaseError: DatabaseError) {

    }

}

    public fun postFirebaseDB(){
        val key = ref.child("user").child(id).child("friends").push().key
        val childUpdate = HashMap<String,Any>()
        val post = FriendItem(name,id,0.0,0.0,0.toString())
        val postValues =post.toMap()

        childUpdate["/user/$id/friends/$key"] = postValues

        ref.updateChildren(childUpdate)
    }

    fun getHashKey(){
        try {
            val info = getPackageManager().getPackageInfo("com.example.a4_1_kt", PackageManager.GET_SIGNATURES);
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray())
                Toast.makeText(applicationContext,""+ Base64.encodeToString(md.digest(), Base64.DEFAULT),Toast.LENGTH_LONG).show()

            }
        } catch (e :PackageManager.NameNotFoundException ) {
            e.printStackTrace();
        } catch (e : NoSuchAlgorithmException) {
            e.printStackTrace();
        }
    }




}
