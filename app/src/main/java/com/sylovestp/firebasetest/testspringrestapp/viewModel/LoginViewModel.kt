package com.sylovestp.firebasetest.testspringrestapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylovestp.firebasetest.testspringrestapp.repository.LoginRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    fun login(memberId: String, memberPw: String) {
        viewModelScope.launch {
            val success = loginRepository.login(memberId, memberPw)
            _loginResult.value = success
        }
    }
}