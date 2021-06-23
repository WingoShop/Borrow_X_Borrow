package com.nywangga.borrowxborrow

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var register: TextView
    private var mAuth: FirebaseAuth? = null
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    lateinit var tvProfileName : TextView
    lateinit var tvLogout : TextView
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        progressBar = findViewById(R.id.progressBar)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileName.visibility = View.GONE
        tvLogout = findViewById(R.id.tvLogout)
        tvLogout.visibility = View.GONE
        register =findViewById(R.id.tvRegister)
        register.setOnClickListener {
            val intent = Intent(this, RegisterUser::class.java)
            startActivityForResult(intent, 248)
        }

        val currentUser = mAuth?.currentUser

        if (currentUser != null) {
            val intent = Intent(this, Posts::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            var email = etEmail.text.toString().lowercase().trim()
            var password = etPassword.text.toString().trim()

            if(email.isEmpty()) {
                etEmail.error = "Please enter valid email!"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter valid email!"
                etEmail.requestFocus()
                return@setOnClickListener

            }

            if(password.isEmpty()) {
                etPassword.error = "Please enter valid password!"
                etPassword.requestFocus()
                return@setOnClickListener

            }
            if (password.length < 6) {
                etPassword.error = "Password minimum is 6 characters!"
                etPassword.requestFocus()
                return@setOnClickListener

            }
            progressBar.visibility = View.VISIBLE
            mAuth!!.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //redirect to main page

                        val currentUser = mAuth!!.currentUser
                        val uid =currentUser?.uid.toString()
                        db.collection("user").document(uid).get()
                            .addOnSuccessListener { documentSnapshot ->
                                val user = documentSnapshot.toObject<User>()
                                if(user?.pair == null) {
                                    val intent = Intent(this, Profile::class.java)
                                    intent.putExtra("NAME", user?.name)
                                    intent.putExtra("UID", uid)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val intent = Intent(this, Posts::class.java)
                                    intent.putExtra("NAME", user?.name)
                                    intent.putExtra("UID", uid)
                                    startActivity(intent)
                                    finish()
                                }

                            }


                        progressBar.visibility = View.GONE

                    } else {
                        Toast.makeText(this, "Failed to login! Try again later!", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                    }

                }
        }


    }


}