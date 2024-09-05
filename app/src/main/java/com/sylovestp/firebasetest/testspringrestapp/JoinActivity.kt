package com.sylovestp.firebasetest.testspringrestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityJoinBinding
import com.sylovestp.firebasetest.testspringrestapp.dto.UserDTO
import com.sylovestp.firebasetest.testspringrestapp.retrofit.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinBtn.setOnClickListener {
            val memberId = binding.userUsername.text.toString()
            val memberName = binding.userName.text.toString()
            val memberPw = binding.userPassword1.text.toString()
            val memberPw2 = binding.userPassword2.text.toString()

            // 비밀번호 확인
            if (memberPw != memberPw2) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 필드 확인
            if (memberId.isEmpty() || memberName.isEmpty() || memberPw.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UserDTO로 데이터 생성
            val userDTO = UserDTO(memberId = memberId, memberName = memberName, memberPw = memberPw)

            // Retrofit 네트워크 서비스
            val networkService = (applicationContext as MyApplication).networkService
            val call = networkService.registerUser(userDTO)
            call.enqueue(object : Callback<UserDTO> {
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@JoinActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@JoinActivity, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    Toast.makeText(this@JoinActivity, "요청 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 로그인 화면으로 이동
        binding.loginBtn.setOnClickListener {
            val intent = Intent(this@JoinActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
