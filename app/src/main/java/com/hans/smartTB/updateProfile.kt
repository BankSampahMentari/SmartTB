package com.hans.smartTB

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hans.smartTB.databinding.ActivityUpdateProfileBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import com.yalantis.ucrop.UCrop


class updateProfile : AppCompatActivity() {
    private lateinit var foto:String
    private lateinit var urifoto:Uri
    private lateinit var hasilGender: String
    private lateinit var binding: ActivityUpdateProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //memasukkan data saat ini ke form
        val bundle = intent.extras
        if (bundle != null) {
            val namaUpdate = bundle.getString("nama")?.trim()
            val emailUpdate = bundle.getString("email")?.trim()
            val genderUpdate = bundle.getString("gender")?.trim()
            val phoneUpdate = bundle.getString("phone")?.trim()

            binding.updateNama.setText("$namaUpdate")
            binding.updateEmail.setText("$emailUpdate")
            binding.updateNomor.setText("$phoneUpdate")
            if (genderUpdate.equals("Pria")){
                binding.rgGender.check(R.id.male)
            }else{
                binding.rgGender.check(R.id.female)
            }

            foto = bundle.getString("foto")!!
            if (foto != ""){
                Glide.with(this).load(bundle?.getString("foto")).into(binding.imageProfile)
            }

        }

        //cek permission camera
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )!= PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }else{
            binding.imageProfile.isEnabled = true
        }

        //mengganti foto
        binding.imageProfile.setOnClickListener{
            selectImage()
        }

        //kirim data update ke firestore
        binding.buttonUpdate.setOnClickListener{
            updateData()
        }

    }

    private fun selectImage() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setItems(items) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 10)
            } else if (items[item] == "Choose from Library") {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 20)
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //import gambar dari galeri
        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            //crop
            path?.let { startCrop(it) }
        }

        //kamera
        if (requestCode == 10 && resultCode == RESULT_OK) {
            val extras = data!!.extras
            val bitmap = extras!!["data"] as Bitmap?
            val imageUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null))
            val path = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))

            //crop
            val options = UCrop.Options()
            options.setCompressionQuality(80)
            options.setToolbarTitle(getString(R.string.app_name))
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            options.setToolbarWidgetColor(Color.WHITE)
            options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            UCrop.of(imageUri!!, path)
                .withAspectRatio(1f, 1f)
                .withOptions(options)
                .start(this)

        }

        //menangkap hasil cropping dan update imageview
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            try {
                val inputStream = contentResolver.openInputStream(resultUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageProfile.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, cropError?.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCrop(it: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))
        val options = UCrop.Options()
        options.setCompressionQuality(80)
        options.setToolbarTitle(getString(R.string.app_name))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setToolbarWidgetColor(Color.WHITE)
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        UCrop.of(it!!, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .start(this)
    }

    //update data
    private fun updateData() {
        val cekGenderRadioButtonId = binding.rgGender.checkedRadioButtonId
        val listGender = findViewById<RadioButton>(cekGenderRadioButtonId)
        hasilGender = "${listGender.text}"

        val edNama = binding.updateNama.text.toString().trim()
        val edEmail = binding.updateEmail.text.toString().trim()
        val edPhone = binding.updateNomor.text.toString().trim()
        val edGender = hasilGender.trim()

        binding.imageProfile.isDrawingCacheEnabled = true
        binding.imageProfile.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(binding.imageProfile.drawingCache)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        //UPLOAD progress popup
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        //proses upload
        val storage = FirebaseStorage.getInstance()
        val reference = storage.getReference("images_user").child("IMG"+ Date().time +".jpeg")
        var uploadTask = reference.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            if(taskSnapshot.metadata !=null){
                if(taskSnapshot.metadata!!.reference !=null){
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener {
                        var editfoto = it.getResult().toString()
                        //menghapus foto lama
                        FirebaseStorage.getInstance().getReferenceFromUrl(foto).downloadUrl
                            .addOnSuccessListener {
                                FirebaseStorage.getInstance().getReferenceFromUrl(foto).delete()
                            }
                            .addOnFailureListener{
                                Toast.makeText(this, "File Not Exist, adding . . .", Toast.LENGTH_SHORT).show()
                            }

                        //bahan update
                        val dbupdate = FirebaseFirestore.getInstance()
                        val bahanProfile = hashMapOf<String, Any>(
                            "name" to edNama,
                            "phone" to edPhone,
                            "gender" to edGender,
                            "foto" to editfoto,
                        )
                        val auth = FirebaseAuth.getInstance()
                        val email = auth.currentUser?.email
                        dbupdate.collection("users").document(email!!).update(bahanProfile)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(this, "Berhasil Memperbarui Profil", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { exception ->
                                dialog.dismiss()
                                Toast.makeText(this, "Failed!, gagal $exception", Toast.LENGTH_SHORT).show()
                            }
                    }
                }else{
                    dialog.dismiss()
                    Toast.makeText(this, "Failed 1!", Toast.LENGTH_SHORT).show()
                }
            }else{
                dialog.dismiss()
                Toast.makeText(this, "Failed 2!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}