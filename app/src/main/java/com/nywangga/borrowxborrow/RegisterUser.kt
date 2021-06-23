package com.nywangga.borrowxborrow

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterUser : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    //private lateinit var tvBanner: TextView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword : EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    lateinit var tvProfileName : TextView
    lateinit var tvLogout : TextView

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        progressBar = findViewById(R.id.progressBar)

        mAuth = FirebaseAuth.getInstance()

        //tvBanner = findViewById(R.id.tvBanner)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        btnRegister = findViewById(R.id.btnLogin)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileName.visibility = View.GONE
        tvLogout = findViewById(R.id.tvLogout)
        tvLogout.visibility = View.GONE

//        tvBanner.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivityForResult(intent, 249)
//        }
        btnRegister.setOnClickListener {
            var fullName = etFullName.text.toString().trim()
            var email = etEmail.text.toString().lowercase().trim()
            var password = etPassword.text.toString().trim()

            if(fullName.isEmpty()) {
                etFullName.error = "Please enter full name!"
                etFullName.requestFocus()
                return@setOnClickListener
            }
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
            mAuth!!.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { authTask ->
                    if (authTask.isSuccessful) {
                        val user = mAuth!!.currentUser
                        val userDb = User(fullName,email)
                        val uid =user?.uid.toString()
                        db.collection("user").document(uid)
                            .set(userDb)
                            .addOnCompleteListener { toDbTask ->
                                if (toDbTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "User has been registered successfully! ${user?.uid}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(this, Profile::class.java)
                                    intent.putExtra("NAME", fullName)
                                    intent.putExtra("UID", uid)
                                    startActivity(intent)
                                    finish()
                                    progressBar.visibility = View.GONE
                                } else {
                                    Toast.makeText(this, "Failed to register! Try again!", Toast.LENGTH_LONG).show()
                                    progressBar.visibility = View.GONE

                                }
                            }


                    } else {
                        Toast.makeText(this, "Failed to register! Try again!", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE

                    }
                }
        }

    }
}