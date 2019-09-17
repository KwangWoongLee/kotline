package com.example.a4_1_kt

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var googleSignInClient:GoogleSignInClient
    val RC_SIGN_IN = 900
    lateinit var firebaseAuth:FirebaseAuth
    lateinit var progressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = ProgressDialog(this)


//
//        if(firebaseAuth.getCurrentUser() !=null){
//            val intent = Intent(this,MainActivity::class.java)
//            finish()
//            startActivity(intent)
//        }

        var googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);


        btn_Login.setOnClickListener(View.OnClickListener {
            var intent = googleSignInClient.getSignInIntent()
            startActivityForResult(intent,RC_SIGN_IN)
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
                val account:GoogleSignInAccount = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)

        }catch (e:ApiException ){
            Toast.makeText(applicationContext, "에러" + e, Toast.LENGTH_SHORT).show()
        }



    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken,null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, OnCompleteListener<AuthResult>(){
                if(it.isSuccessful){
//                    progressDialog.setMessage("로그인 중입니다...")
//                    progressDialog.show()
                    val intent = Intent(this,MainActivity::class.java)
                    intent.putExtra("photoUrl",acct.photoUrl.toString())
                    intent.putExtra("id",acct.id)
                    intent.putExtra("name",acct.displayName)
                    intent.putExtra("email",acct.email)
                    startActivity(intent)
                }
                else
                    Toast.makeText(applicationContext, "로그인실패", Toast.LENGTH_SHORT).show()
            })

    }





}
