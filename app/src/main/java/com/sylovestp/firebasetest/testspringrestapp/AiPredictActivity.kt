package com.sylovestp.firebasetest.testspringrestapp

import android.content.Intent
import android.graphics.Bitmap
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityAiPredictBinding
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityJoinBinding
import com.sylovestp.firebasetest.testspringrestapp.dto.UserDTO
import com.sylovestp.firebasetest.testspringrestapp.retrofit.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class AiPredictActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null  // Nullable URI

    private val selectImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data!!
//            imageView.setImageURI(imageUri)
            // 코너 반경 (px 단위)
            val cornerRadius = 500


// 이미지 로드 및 코너 둥글게 적용
            Glide.with(this)
                .load(imageUri) // 이미지 URL 또는 로컬 리소스
                .apply(RequestOptions().transform(RoundedCorners(cornerRadius)))
                .into(imageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityAiPredictBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.userProfile2


        binding.userProfile2.setOnClickListener {
            openGallery()
        }

        binding.predictSendBtn.setOnClickListener {

           imageUri?.let { it1 -> processImage(it1) }

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }// onCreate

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    // 이미지 처리 후, 서버로 전송하는 함수
    private fun processImage(uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 1. JSON 데이터 생성
//                val userRequestBody = createRequestBodyFromDTO(userDTO)

                // 2. 이미지 축소 및 MultipartBody.Part 생성
                val resizedBitmap = getResizedBitmap(uri, 200, 200) // 200x200 크기로 축소
                val imageBytes = bitmapToByteArray(resizedBitmap)
                val profileImagePart = createMultipartBodyFromBytes(imageBytes)

                // 3. 서버로 전송
                uploadData(profileImagePart)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AiPredictActivity, "Image processed successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AiPredictActivity, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } // 함수끝

    // 함수 사이즈 조절, 썸네일 이미지 압축
    private suspend fun getResizedBitmap(uri: Uri, width: Int, height: Int): Bitmap {
        return withContext(Dispatchers.IO) {
            val futureTarget: FutureTarget<Bitmap> = Glide.with(this@AiPredictActivity)
                .asBitmap()
                .load(uri)
                .override(width, height)  // 지정된 크기로 축소
                .submit()

            // Bitmap을 반환
            futureTarget.get()
        }
    } // 함수 끝

    // 이미지 타입 , 비트맵 -> 바이트 단위로 변경.
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // 압축 품질을 80%로 설정
        return byteArrayOutputStream.toByteArray()
    }// 함수끝


    private fun uploadData(profileImage: MultipartBody.Part?) {
        val networkService = (applicationContext as MyApplication).networkService
        val call = networkService.predictImage(profileImage)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AiPredictActivity, "서버 전송 성공", Toast.LENGTH_SHORT).show()
                        Log.d("lsy","${response}")
                } else {
                    Toast.makeText(this@AiPredictActivity, "Failed to create user: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AiPredictActivity, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    } // 함수끝

    fun createRequestBodyFromDTO(userDTO: UserDTO): RequestBody {
        val gson = Gson()
        val json = gson.toJson(userDTO)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }// 함수끝


    private fun createMultipartBodyFromBytes(imageBytes: ByteArray): MultipartBody.Part {
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
        return MultipartBody.Part.createFormData("profileImage", "image.jpg", requestFile)
    } // 함수끝

}