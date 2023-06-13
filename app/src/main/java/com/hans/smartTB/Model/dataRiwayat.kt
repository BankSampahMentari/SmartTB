package com.hans.smartTB.Model

import android.widget.TextView
import com.google.firebase.firestore.DocumentId
import com.hans.smartTB.R
import org.w3c.dom.Text
import java.sql.Timestamp

class dataRiwayat {
    val namaNasabah : String? = null
    val tanggal : com.google.firebase.Timestamp? = null
    val status : String? = null
    val pendapatan : String? = null
    val email: String? = null
    @DocumentId
    val docID : String = ""
}