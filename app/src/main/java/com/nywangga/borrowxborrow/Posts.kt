package com.nywangga.borrowxborrow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.util.*

class Posts : AppCompatActivity() {

    private lateinit var tvBanner: TextView
    private lateinit var tvProfileName: TextView
    private val db = Firebase.firestore
    private var mAuth: FirebaseAuth? = null
    private var uid: String = ""
    private var name: String = ""
    private var email = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBorrow: Button
    private lateinit var btnPaid: Button
    private var pair = ""
    private lateinit var etAmount: EditText
    private lateinit var etRemark: EditText
    private lateinit var tvCurrentBalance: TextView
    private lateinit var firstDoc: Post
    private var createdBy = ""
    private lateinit var rvHistory: RecyclerView
    private var postHistoryList = mutableListOf<Post>()
    private lateinit var tvLogout: TextView

    private var realEmail: String = ""
    private var realPair = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        progressBar = findViewById(R.id.progressBar)
        btnBorrow = findViewById(R.id.btnBorrow)
        mAuth = FirebaseAuth.getInstance()
        tvBanner = findViewById(R.id.tvBannerHead)
        btnPaid = findViewById(R.id.btnPaid)
        rvHistory = findViewById(R.id.rvHistory)
        tvLogout = findViewById(R.id.tvLogout)

        tvProfileName = findViewById(R.id.tvProfileName)
        etAmount = findViewById(R.id.etAmount)
        etRemark = findViewById(R.id.etRemark)
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance)
        val currentUser = mAuth?.currentUser

        if (currentUser != null) {
            uid = currentUser.uid.toString()
            email = currentUser.email.toString()
            db.collection("user").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject<User>()
                    name = user?.name.toString()
                    email = user?.email.toString()
                    pair = user?.pair.toString()
                    tvProfileName.text = name
                    realEmail = email
                    realPair = pair

                    val documentList = listOf(email, pair).sorted()
                    createdBy = email

                    email = documentList[0]
                    pair = documentList[1]
                    val displayPostsRef = db.collection("posts")
                    val displayPosts = displayPostsRef
                        .whereEqualTo("email_one", email)
                        .whereEqualTo("email_two", pair)
                        .orderBy("created_date", Query.Direction.DESCENDING)
                        .limit(50)
                        .get()
                        .addOnSuccessListener { docz ->
                            firstDoc = docz.documents[0].toObject<Post>()!!
                            var displayBalance = firstDoc!!.balance
                            if (createdBy == pair) {
                                displayBalance = displayBalance!!.toInt() * -1
                            }
                            tvCurrentBalance.text = "Current Balance: Rp $displayBalance"

                            for (doc in docz.documents) {
                                postHistoryList.add(doc.toObject<Post>()!!)
                            }
                            rvHistory.adapter = PostHistoryAdapter(this, postHistoryList)
                            rvHistory.setHasFixedSize(true)
                            rvHistory.layoutManager = LinearLayoutManager(this)
                        }
                }
        }

        btnPaid.setOnClickListener {
            val uuid = UUID.randomUUID().toString()
            var postDb = Post(email,pair,firstDoc.balance,0,"Paid",createdBy)
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

                    } else {
                        Toast.makeText(this, "Initialization failed! Try again!", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE

                    }
                }
        }


        tvBanner.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivityForResult(intent, 249)
            finish()
        }

        tvLogout.setOnClickListener {
            mAuth!!.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivityForResult(intent, 249)
            finish()
        }



        tvProfileName.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("NAME", name)
            intent.putExtra("UID", uid)
            startActivity(intent)
            finish()
        }

        btnBorrow.setOnClickListener {

            var amount = etAmount.text.toString().trim()
            var remarks = etRemark.text.toString().trim()

            if (amount.isEmpty() || amount.toInt() < 1) {
                etAmount.error = "Please enter a valid amount"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            var amount2 = amount.toInt()


            val documentList = listOf(realEmail, realPair).sorted()
            val createdByReal = realEmail

            email = documentList[0]
            pair = documentList[1]
            val uuid = UUID.randomUUID().toString()
            val postsRef = db.collection("posts")
            val postz = postsRef
                .whereEqualTo("email_one", email)
                .whereEqualTo("email_two", pair)
                .orderBy("created_date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { docz ->
                    for (document in docz.documents) {
                        val docu = document.toObject<Post>()
                        var newBalance: Int = 0
                        if (createdByReal == email) {
                            newBalance = docu?.balance!! - amount2

                        } else if (createdByReal == pair) {
                            newBalance = docu?.balance!! + amount2
                        }
                        val postDb = Post(email, pair, amount2, newBalance, remarks, createdByReal)
                        db.collection("posts").document(uuid)
                            .set(postDb)
                            .addOnCompleteListener { toDbTask ->
                                if (toDbTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Balance updated",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    progressBar.visibility = View.GONE
                                    val intent = Intent(this, Posts::class.java)
                                    startActivity(intent)
                                    finish()

                                } else {
                                    Toast.makeText(
                                        this,
                                        "Initialization failed! Try again!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    progressBar.visibility = View.GONE

                                }
                            }
                    }
                }
        }
    }
}