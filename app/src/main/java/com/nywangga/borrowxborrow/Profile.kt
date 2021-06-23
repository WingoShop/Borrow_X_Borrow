package com.nywangga.borrowxborrow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.UUID

class Profile : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var etName: EditText
    private lateinit var etPair: EditText
    private lateinit var tvBanner: TextView
    private lateinit var btnUpdate: Button
    private val db = Firebase.firestore
    private lateinit var tvProfileName: TextView

    private var uid: String = ""
    private var currentName = ""
    private var email = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLogout: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        progressBar = findViewById(R.id.progressBar)
        mAuth = FirebaseAuth.getInstance()
        etName = findViewById(R.id.etName)
        etPair = findViewById(R.id.etPair)
        btnUpdate = findViewById(R.id.btnUpdate)
        tvBanner = findViewById(R.id.tvBannerHead)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvLogout = findViewById(R.id.tvLogout)

        tvBanner.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivityForResult(intent, 249)
            finish()
        }

        val currentUser = mAuth?.currentUser


        if (currentUser != null) {
            uid = currentUser.uid.toString()
            email = currentUser.email.toString()
            db.collection("user").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject<User>()
                    currentName = user?.name.toString()
                    tvProfileName.text = currentName
                    etName.setText(currentName)
                    val currentPartner = user?.pair.toString()
                    etPair.setText(currentPartner)
                }
        }

        tvLogout.setOnClickListener {
            mAuth!!.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivityForResult(intent, 249)
            finish()
        }

        btnUpdate.setOnClickListener {
            var name = etName.text.toString().trim()
            var pair = etPair.text.toString().lowercase().trim()

            if (name.isEmpty()) {
                etName.error = "Please enter full name!"
                etName.requestFocus()
            }
            if (pair.isEmpty()) {
                etPair.error = "Please enter valid email for the pair!"
                etPair.requestFocus()

            }
            if (!Patterns.EMAIL_ADDRESS.matcher(pair).matches()) {
                etPair.error = "Please enter valid email for the pair!"
                etPair.requestFocus()

            }
            progressBar.visibility = View.VISIBLE
            db.collection("user").document(uid).update(mapOf(
                "name" to name,
                "pair" to pair
            ))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentList = listOf(email,pair).sorted()
                    email = documentList[0]
                    pair = documentList[1]
                    val uuid = UUID.randomUUID().toString()
                    val postsRef = db.collection("posts")
                    val postz = postsRef.whereEqualTo("email_one", email).whereEqualTo("email_two", pair).get()
                        .addOnSuccessListener { docz ->
                            if (docz.isEmpty) {
                                var postDb = Post(email,pair,0,0,"Initialization",email)
                                db.collection("posts").document(uuid)
                                    .set(postDb)
                                    .addOnCompleteListener { toDbTask ->
                                        if (toDbTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Balance initialized",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            progressBar.visibility = View.GONE
                                            val intent = Intent(this, Posts::class.java)
                                            startActivity(intent)
                                            finish()

                                        } else {
                                            Toast.makeText(this, "Initialization failed! Try again!", Toast.LENGTH_LONG).show()
                                            progressBar.visibility = View.GONE

                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Profile Updated",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this, Posts::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }

                }
            }


        }
    }
}