package com.sylovestp.firebasetest.testspringrestapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
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
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityJoinBinding
import com.sylovestp.firebasetest.testspringrestapp.databinding.ItemViewBinding
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


class JoinActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var binding: ActivityJoinBinding
    private var imageUri: Uri? = null  // Nullable URI
    // ActivityResultLauncher 선언
    private lateinit var addressFinderLauncher: ActivityResultLauncher<Bundle>

    private val selectImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data!!
//            imageView.setImageURI(imageUri)
            // 코너 반경 (px 단위)
//            val cornerRadius = 500


// 이미지 로드 및 코너 둥글게 적용
            Glide.with(this)
                .load(imageUri) // 이미지 URL 또는 로컬 리소스
                .apply(RequestOptions().circleCrop())
                .into(imageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJoinBinding.inflate(layoutInflater)
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
            val memberId = binding.userUsername.text.toString()
            val memberName = binding.userName.text.toString()
            val memberPw = binding.userPassword1.text.toString()
            val userDTO = UserDTO(memberId,memberName,memberPw)
            Toast.makeText(this@JoinActivity, "${memberId}, ${memberPw}, ${imageUri}", Toast.LENGTH_SHORT).show()
            if (userDTO != null) {
                // 회원가입시,
                //
                imageUri?.let { it1 -> processImage(userDTO, it1) }
            }
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this@JoinActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        // ActivityResultLauncher 등록
//        addressFinderLauncher = registerForActivityResult(AddressFinder.contract) { result ->
//            if (result != Bundle.EMPTY) {
//                // address와 zipcode 값을 받아온 후 처리
//                val address = result.getString(AddressFinder.ADDRESS)
//                val zipCode = result.getString(AddressFinder.ZIPCODE)
//                val editableText: Editable = Editable.Factory.getInstance().newEditable("[$zipCode] $address")
//                // 받은 데이터를 사용해 필요한 작업 수행
//                binding.userAddress.text = editableText
//            }
//        }
//
//        // 버튼 클릭 리스너 설정
//        binding.findAddressBtn.setOnClickListener {
//            // AddressFinder 액티비티 시작
//            addressFinderLauncher.launch(Bundle())
//        }



    } //onCreate

    private fun uploadData(user: RequestBody, profileImage: MultipartBody.Part?) {
        val networkService = (applicationContext as MyApplication).networkService
        val call = networkService.registerUser(user, profileImage)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@JoinActivity, "User created successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@JoinActivity, "Failed to create user: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@JoinActivity, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createRequestBodyFromDTO(userDTO: UserDTO): RequestBody {
        val gson = Gson()
        val json = gson.toJson(userDTO)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun createMultipartBodyFromBytes(imageBytes: ByteArray): MultipartBody.Part {
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
        return MultipartBody.Part.createFormData("profileImage", "image.jpg", requestFile)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun processImage(userDTO: UserDTO, uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 1. JSON 데이터 생성
                val userRequestBody = createRequestBodyFromDTO(userDTO)

                // 2. 이미지 축소 및 MultipartBody.Part 생성
                val resizedBitmap = getResizedBitmap(uri, 200, 200) // 200x200 크기로 축소
                val imageBytes = bitmapToByteArray(resizedBitmap)
                val profileImagePart = createMultipartBodyFromBytes(imageBytes)

                // 3. 서버로 전송
                uploadData(userRequestBody, profileImagePart)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@JoinActivity, "Image processed successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@JoinActivity, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getResizedBitmap(uri: Uri, width: Int, height: Int): Bitmap {
        return withContext(Dispatchers.IO) {
            val futureTarget: FutureTarget<Bitmap> = Glide.with(this@JoinActivity)
                .asBitmap()
                .load(uri)
                .override(width, height)  // 지정된 크기로 축소
                .submit()

            // Bitmap을 반환
            futureTarget.get()
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // 압축 품질을 80%로 설정
        return byteArrayOutputStream.toByteArray()
    }


}