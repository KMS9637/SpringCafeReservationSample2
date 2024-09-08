package com.sylovestp.firebasetest.testspringrestapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sylovestp.firebasetest.testspringrestapp.JoinActivity
import com.sylovestp.firebasetest.testspringrestapp.MainActivity
import com.sylovestp.firebasetest.testspringrestapp.R
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityLoginBinding
import com.sylovestp.firebasetest.testspringrestapp.repository.LoginRepository
import com.sylovestp.firebasetest.testspringrestapp.retrofit.INetworkService
import com.sylovestp.firebasetest.testspringrestapp.retrofit.MyApplication
import com.sylovestp.firebasetest.testspringrestapp.viewModel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var apiService: INetworkService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myApplication = application as MyApplication
        myApplication.initialize(this)  // Initialize the application
        apiService = myApplication.getApiService()
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // ViewModel 초기화
        loginViewModel = LoginViewModel(LoginRepository(apiService, sharedPreferences))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginJoinBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, JoinActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginLoginBtn.setOnClickListener {
            val memberId = binding.loginUsername.text.toString()
            val memberPw = binding.loginPassword.text.toString()

            loginViewModel.login(memberId, memberPw)
        }

        loginViewModel.loginResult.observe(this) { loginResponse ->
            if (loginResponse != null) {
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                Log.d("lsy", "loginResponse.memberNo : ${loginResponse.memberId}")
                saveMemberNo(loginResponse.memberId)
                Log.d("LoginActivity", "서버에서 받은 회원 번호: ${loginResponse.memberId}")

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMemberNo(memberId: String) {
        Log.d("LoginActivity", "저장할 회원 번호: $memberId")
        val editor = sharedPreferences.edit()
        editor.putString("memberId", memberId)
        editor.apply()

        Log.d("LoginActivity", "회원 번호 저장됨: $memberId")
    }
}
