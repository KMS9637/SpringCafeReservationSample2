package com.sylovestp.firebasetest.testspringrestapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityProfileBinding
import com.sylovestp.firebasetest.testspringrestapp.repository.LoginRepository
import com.sylovestp.firebasetest.testspringrestapp.retrofit.INetworkService
import com.sylovestp.firebasetest.testspringrestapp.retrofit.MyApplication
import com.sylovestp.firebasetest.testspringrestapp.viewModel.LoginViewModel
import com.sylovestp.firebasetest.testspringrestapp.viewModelFactory.LoginViewModelFactory

class ProfileActivity : AppCompatActivity() {
    private lateinit var networkService: INetworkService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ViewModel 초기화
        val myApplication = application as MyApplication
        networkService = myApplication.getApiService()
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val repository = LoginRepository(networkService, sharedPreferences)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(repository)).get(LoginViewModel::class.java)

        val deleteButton: ImageButton = findViewById(R.id.btn_delete_account)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("계정 삭제")
                .setMessage("정말로 계정을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { dialog, which ->
                    val memberId = getMemberId()
                    val token = getJwtToken()

                    if (memberId != null && token != null) {
                        // 삭제 요청
                        loginViewModel.deleteUser(memberId, token)
                    } else {
                        Toast.makeText(this, "회원 정보나 토큰을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 닉네임 수정 기능
        findViewById<ImageButton>(R.id.btn_edit_nickname).setOnClickListener {
            val editText = EditText(this)
            AlertDialog.Builder(this)
                .setTitle("닉네임 수정")
                .setView(editText)
                .setPositiveButton("저장") { dialog, which ->
                    val newNickname = editText.text.toString()
                    findViewById<TextView>(R.id.tv_nickname).text = newNickname
                    // 서버에 닉네임 업데이트 요청 추가 가능
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 소개 키워드 수정 기능
        findViewById<ImageButton>(R.id.btn_edit_intro_keyword).setOnClickListener {
            val editText = EditText(this)
            AlertDialog.Builder(this)
                .setTitle("소개 키워드 수정")
                .setView(editText)
                .setPositiveButton("저장") { dialog, which ->
                    val newKeyword = editText.text.toString()
                    findViewById<TextView>(R.id.tv_intro_keyword).text = newKeyword
                    // 서버에 소개 키워드 업데이트 요청 추가 가능
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 회원 삭제 결과 옵저빙
        loginViewModel.deleteResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "회원 삭제 성공", Toast.LENGTH_SHORT).show()
                // 성공 시 적절한 후속 처리 (예: 메인 화면으로 이동)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "회원 삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMemberId(): String? {
        val memberId = sharedPreferences.getString("memberId", "123")
        return if (memberId != null) {
            Log.d("ProfileActivity", "회원 번호 로드됨: $memberId")
            memberId
        } else {
            Log.e("ProfileActivity", "회원 번호가 SharedPreferences에 없습니다.")
            null
        }
    }

    private fun getJwtToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }
}
