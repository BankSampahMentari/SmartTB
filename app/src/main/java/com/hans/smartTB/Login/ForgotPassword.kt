package com.hans.smartTB.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.hans.smartTB.MainActivity
import com.hans.smartTB.R
import com.hans.smartTB.databinding.ActivityForgotPasswordBinding

class ForgotPassword : AppCompatActivity() {
    lateinit var binding:ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityForgotPasswordBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, loginpage::class.java))
        }

        binding.edtEmailReset.setText(intent.getStringExtra("email"))
        binding.btnReset.setOnClickListener {
            val email = binding.edtEmailReset.text.toString()
            val edtEmail= binding.edtEmailReset

            // jika email kosong
            if (email.isEmpty()){
                edtEmail.error= "Email Tidak Boleh Kosong"
                edtEmail.requestFocus()
                return@setOnClickListener
            }

            // Jika email tidak valid
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edtEmail.error= "Email Tidak Valid"
                edtEmail.requestFocus()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Email Reset Password Telah Dikirim", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, loginpage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}