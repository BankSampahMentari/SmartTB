package com.hans.smartTB

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.hans.smartTB.Fragment.riwayat
import com.hans.smartTB.Login.loginpage
import com.hans.smartTB.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    lateinit var auth: FirebaseAuth
    private lateinit var foto : String
    lateinit var database: FirebaseDatabase
    lateinit var pendingIntent: PendingIntent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Langkah 1: Menghubungkan aplikasi Android ke Realtime Database Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        //intent dari notifikasi
        val intent = Intent(this, MainActivity::class.java)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //fungsi utama
        fetchData()

        binding.mapView.onCreate(savedInstanceState)
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

        //Harga Sampah
        val harga = findViewById<CardView>(R.id.cvHarga)
        harga.setOnClickListener {
            val intent = Intent(this@MainActivity, hargaSampah::class.java)
            startActivity(intent)
        }

        val kategori = findViewById<CardView>(R.id.cvKategori)
        kategori.setOnClickListener{
            val intent = Intent(this@MainActivity, jenisSampah::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    private fun hidefragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.hide(fragment)
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commit()
    }

    private fun logout() {
        showLogoutConfirmationDialog()
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Konfirmasi Keluar")
        alertDialogBuilder.setMessage("Apakah Anda yakin ingin log out?")

        val positiveText = "Logout"
        val neutralText = "Keluar Aplikasi"
        val negativeText = "Batal"

        // Membuat SpannableString untuk menerapkan warna pada teks pilihan
        val spannablePositive = SpannableString(positiveText)
        spannablePositive.setSpan(
            ForegroundColorSpan(Color.RED),
            0,
            positiveText.length,
            0
        )

        val spannableNeutral = SpannableString(neutralText)
        spannableNeutral.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF9800")),
            0,
            neutralText.length,
            0
        )

        val spannableNegative = SpannableString(negativeText)
        spannableNegative.setSpan(
            ForegroundColorSpan(Color.parseColor("#42b752")),
            0,
            negativeText.length,
            0
        )

        alertDialogBuilder.setPositiveButton(spannablePositive) { dialog: DialogInterface, _: Int ->
            // Tindakan yang akan dilakukan jika tombol "Ya" ditekan
            auth.signOut()
            val intent = Intent(this, loginpage::class.java)
            startActivity(intent)
        }

        alertDialogBuilder.setNeutralButton(spannableNeutral) { dialog: DialogInterface, _: Int ->
            finishAffinity()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton(spannableNegative) { dialog: DialogInterface, _: Int ->
            // Tindakan yang akan dilakukan jika tombol "Batal" ditekan
            dialog.dismiss()
        }

        // Membuat bentuk dengan radius 20dp dan background berwarna putih
        val radius = resources.getDimensionPixelSize(R.dimen.dialog_corner_radius).toFloat()
        val outerRadii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        val shapeDrawable = ShapeDrawable(RoundRectShape(outerRadii, null, null))
        shapeDrawable.paint.color = Color.WHITE
        shapeDrawable.paint.style = Paint.Style.FILL

        // Membuat drawable dengan stroke berwarna hijau
        val strokeWidth = 10f
        val strokeColor = Color.parseColor("#42b752")
        val strokeDrawable = ShapeDrawable(RoundRectShape(outerRadii, null, null))
        strokeDrawable.paint.color = Color.TRANSPARENT
        strokeDrawable.paint.style = Paint.Style.STROKE
        strokeDrawable.paint.strokeWidth = strokeWidth
        strokeDrawable.paint.color = strokeColor

        // Menggabungkan background dan stroke menjadi satu drawable
        val layers: Array<Drawable> = arrayOf(shapeDrawable, strokeDrawable)
        val layerDrawable = LayerDrawable(layers)

        // Mengatur background drawable pada dialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(layerDrawable)
        alertDialog.show()
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
        val email = auth.currentUser?.email!!.lowercase()
        val reference = database.getReference("Node")

        //fetch data user
        var Fstore = FirebaseFirestore.getInstance().collection("users").document(email!!)
        Fstore.get().addOnSuccessListener{
         if (it != null) {
             val name = it.getString("name")
             foto = it.getString("foto").toString()

             binding.tvUsername.text = name
             binding.emailuser.text = "Email : $email"

             Glide.with(this)
                 .load(foto)
                 .into(binding.imageProfile)
         }else{
             Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
         }
        }

        //fetch data alat user
        val dataSensor = reference.orderByChild("Email").equalTo(email)
        dataSensor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Handle case when no matching email is found in the database
                    binding.tvSmartTC.visibility = View.GONE
                    binding.mapView.visibility = View.GONE
                    binding.tvNodeID.visibility = View.GONE
                    binding.ivBaterai.visibility = View.GONE
                    binding.tvBaterai.visibility = View.GONE
                    binding.tvKapasitas.visibility = View.GONE
                    binding.tvLattitude.visibility = View.GONE
                    binding.tvLongitude.visibility = View.GONE
                    binding.tvBateraibar.visibility = View.GONE
                    binding.pbKapasitas.visibility = View.GONE
                    binding.tvProgress.visibility = View.GONE
                    binding.TrashCan.visibility = View.VISIBLE
                } else
                {
                    dataSensor.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val node = snapshot.key
                                binding.tvNodeID.text = "Node ID : $node"

                                //cek kapasitas tempat sampah
                                val jarak = snapshot.child("jarak").getValue(String::class.java)?.toFloat()
                                val Maxsampah = 60
                                if (jarak != null && jarak <= Maxsampah) {
                                    var persentase = (((Maxsampah - jarak!!) / (Maxsampah - 10)) * 100).toInt()
                                    when {
                                        persentase <=1 -> persentase = 1
                                        persentase >= 100 -> persentase = 100
                                    }
                                    binding.pbKapasitas.progress = persentase
                                    binding.tvProgress.text = "$persentase%"
                                    binding.tvKapasitas.text = "Kapasitas Terpakai : $persentase%"
                                }

                                //update GPS
                                val latt = snapshot.child("Lattitude").getValue(String::class.java)
                                val long = snapshot.child("Longitude").getValue(String::class.java)
                                if (latt != null && long != null) {
                                    binding.tvLattitude.text = "lattitude : $latt"
                                    binding.tvLongitude.text = "longitude : $long"
                                    binding.mapView.getMapAsync {
                                        //menyesuaikan tema
                                        val isDarkTheme =
                                            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                                        if (isDarkTheme) {
                                            val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                                applicationContext,
                                                R.raw.map_style_night
                                            )
                                            it.setMapStyle(mapStyleOptions)
                                        } else {
                                            it.setMapStyle(null)
                                        }
                                        //menambahkan titik
                                        Log.d("TAG", "Menampilkan marker pada peta")
                                        val koordinat = com.google.android.gms.maps.model.LatLng(
                                            latt.toDouble(),
                                            long.toDouble()
                                        )
                                        it.addMarker(MarkerOptions().position(koordinat).title("$node"))
                                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(koordinat, 15f))
                                    }
                                }
//                    else binding.mapView.visibility= View.GONE

                                //cek kapasitas baterai
                                val persen =
                                    snapshot.child("Baterai").getValue(String::class.java)?.toFloat()
//                    val persen = ((baterai!! - 3) / 1.2) * 100
                                binding.tvBaterai.text = "Baterai : ${persen?.toInt()}%"
                                binding.tvBateraibar.text = "${persen?.toInt()}%"
                                updateIconBaterai(persen!!.toFloat())
                                if (persen != null && persen <= 15) {
                                    notifikasiBaterai(persen.toInt())
                                }
                                if(persen != null && persen <= 5) {
                                    EmergencyNotification(persen.toInt())
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(
                                "Firebase",
                                "Error reading data from Realtime Database: " + databaseError.message
                            )
                        }

                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error reading data from Realtime Database: " + databaseError.message)
            }
        })

        }


    fun notifikasiBaterai(baterai: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "low_battery_channel"
        val channelName = "Low Battery"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.android_low_battery)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Baterai Lemah")
            .setContentText("Baterai Perangkat Tersisa $baterai%, Mohon Segera Isi Ulang")
            .setContentInfo("Info")
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setSilent(false)
            .setPriority(2)

        notificationManager.notify(1, notificationBuilder.build())
    }

    fun EmergencyNotification(baterai: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "emergency_channel"
        val channelName = "Battery Runs Out"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.android_low_battery)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Baterai Habis")
            .setContentText("Baterai Perangkat Tersisa $baterai%, Perangkat Akan Segera Dinonaktifkan")
            .setContentInfo("Info")
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setSilent(false)
            .setPriority(2)

        notificationManager.notify(2, notificationBuilder.build())
    }

    private fun updateIconBaterai(baterai: Float?) {
        if (baterai != null) {
            when
            {
                baterai <= 7 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_0_bar_24)
                baterai <= 20 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_1_bar_24)
                baterai <= 35 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_2_bar_24)
                baterai <= 50 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_3_bar_24)
                baterai <= 60 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_4_bar_24)
                baterai <= 75 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_5_bar_24)
                baterai <= 87 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_6_bar_24)
                baterai > 87 -> binding.ivBaterai.setImageResource(R.drawable.baseline_battery_full_24)
            }
        }
    }

    override fun onBackPressed() {
        showLogoutConfirmationDialog()
    }

}