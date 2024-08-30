package com.sylovestp.firebasetest.testspringrestapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityAiPredictBinding
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityJoinBinding

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

}