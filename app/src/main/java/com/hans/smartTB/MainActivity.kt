package com.hans.smartTB

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hans.smartTB.Fragment.riwayat
import com.hans.smartTB.Login.loginpage
import com.hans.smartTB.databinding.ActivityMainBinding

lateinit var binding : ActivityMainBinding
lateinit var auth: FirebaseAuth
private lateinit var foto : String

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        fetchData()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //navbar
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    replaceFragment(Fragment())
                    hidefragment(riwayat())
                }
                R.id.riwayat -> {
                    replaceFragment(riwayat())
                }
                R.id.logout -> {
                    logout()
                }
            }
            true
        }

        //lihat profil
        binding.imageProfile.setOnClickListener{
            val intent = Intent(this, user_Profile::class.java)
                .putExtra("name", binding.tvUsername.text.toString().trim())
                .putExtra("email", binding.emailuser.text.toString().trim())
                .putExtra("foto", foto.trim())
            startActivity(intent)
        }



    }

    private fun hidefragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.hide(fragment)
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commit()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, loginpage::class.java)
        startActivity(intent)
    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.show(fragment)
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commit()
    }

    private fun fetchData() {
        val email = auth.currentUser?.email
        var Fstore = FirebaseFirestore.getInstance().collection("users").document(email!!)
        Fstore.get().addOnSuccessListener{
         if (it != null)
         {
             val name = it.getString("name")
             val email = it.getString("email")
             foto = it.getString("foto").toString()

             binding.tvUsername.text = name
             binding.emailuser.text = email

             Glide.with(this)
                 .load(foto)
                 .into(binding.imageProfile)
         }
        }
    }

}