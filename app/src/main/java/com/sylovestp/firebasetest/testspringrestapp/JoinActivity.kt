package com.sylovestp.firebasetest.testspringrestapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityJoinBinding
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityLoginBinding
import com.sylovestp.firebasetest.testspringrestapp.model.UserDTO
import com.sylovestp.firebasetest.testspringrestapp.retrofit.MyApplication
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class JoinActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null  // Nullable URI

    private val selectImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data!!
            imageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.userProfile

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.userProfile.setOnClickListener {
            openGallery()
        }


        binding.joinBtn.setOnClickListener {
            val username = binding.userUsername.text.toString()
            val password = binding.userPassword1.text.toString()
            val email = binding.userEmail.text.toString()
            val userDTO = UserDTO(username,password,email)
            Toast.makeText(this@JoinActivity, "${username}, ${password},${email}, ${imageUri}", Toast.LENGTH_SHORT).show()
            if (userDTO != null) {
                registerUser(userDTO,imageUri)
            }
        }


    } //onCreate

    private fun registerUser(userDTO: UserDTO, imageUri: Uri?) {

        val networkService = (applicationContext as MyApplication).networkService
        val userRequestBody = createRequestBodyFromDTO(userDTO)
//        val filePart = imageUri?.let { createMultipartBodyFromFile(it) }
        // 이미지가 선택된 경우에만 파일을 전송
        val body: MultipartBody.Part? = imageUri?.let {
            val imageBytes = getBytesFromUri(it)
            val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
            MultipartBody.Part.createFormData("profileImage", "image.jpg", requestFile)
        }

        val call = networkService.registerUser(userRequestBody, body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JoinActivity, "User created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@JoinActivity, "Failed to create user: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MainActivity", "Failed to create user", t)
                Toast.makeText(this@JoinActivity, "Failed to create user: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    } //registerUser

    fun createRequestBodyFromDTO(userDTO: UserDTO): RequestBody {
        val gson = Gson()
        val json = gson.toJson(userDTO)
        return RequestBody.create("application/json".toMediaTypeOrNull(), json)
    }

    fun createMultipartBodyFromFile(uri: Uri): MultipartBody.Part? {
        val file = getFileFromUri(uri)
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }


//    private fun getMimeType(uri: Uri): String? {
//        return contentResolver.getType(uri)
//    }
//
    private fun getBytesFromUri(uri: Uri): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        return inputStream?.readBytes() ?: throw IOException("Unable to open InputStream from URI")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun getFileFromUri(uri: Uri): File {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val filePath = cursor.getString(columnIndex)
        cursor.close()
        return File(filePath)
    }


}