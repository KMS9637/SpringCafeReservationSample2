package com.sylovestp.firebasetest.testspringrestapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylovestp.firebasetest.testspringrestapp.dto.LoginResponse
import com.sylovestp.firebasetest.testspringrestapp.repository.LoginRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    // 로그인 결과를 저장할 LiveData
    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> get() = _loginResult

    // 회원 삭제 결과를 저장할 LiveData
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult

    // 로그인 요청
    fun login(memberId: String, memberPw: String) {
        viewModelScope.launch {
            try {
                val response = loginRepository.login(memberId, memberPw)
                _loginResult.value = response
                if (response != null) {
                    Log.d("LoginViewModel", "로그인 성공")
                } else {
                    Log.e("LoginViewModel", "로그인 실패")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "로그인 오류", e)
                _loginResult.value = null
            }
        }
    }

    // 회원 삭제 요청
    fun deleteUser(memberId: String, token: String) {
        viewModelScope.launch {
            try {
                val response: Response<Unit> = loginRepository.deleteUser("Bearer $token", memberId)
                _deleteResult.value = response.isSuccessful
                if (response.isSuccessful) {
                    Log.d("LoginViewModel", "회원 삭제 성공")
                } else {
                    Log.e("LoginViewModel", "회원 삭제 실패: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "회원 삭제 오류", e)
                _deleteResult.value = false
            }
        }
    }
}
