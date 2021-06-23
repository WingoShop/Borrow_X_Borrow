package com.nywangga.borrowxborrow

import com.google.firebase.Timestamp


data class Post(
    var email_one: String? = null,
    var email_two: String? = null,
    var amount: Int? = null,
    var balance: Int? = null,
    var remarks: String? = null,
    var created_by: String? = null,
    var created_date: Timestamp? = Timestamp.now()
)
