package com.sylovestp.firebasetest.testspringrestapp.repository

import android.content.SharedPreferences
import android.util.Log
import com.sylovestp.firebasetest.testspringrestapp.dto.LoginRequest
import com.sylovestp.firebasetest.testspringrestapp.retrofit.INetworkService

class LoginRepository(private val apiService: INetworkService, private val sharedPreferences: SharedPreferences) {

    suspend fun login(memberId: String, memberPw: String): Boolean {
        val loginRequest = LoginRequest(memberId, memberPw)
        val response = apiService.login(loginRequest)

        return if (response.isSuccessful && response.body() != null) {
            val accessToken = response.body()?.accessToken
            val refreshToken = response.body()?.refreshToken
            val responseMemberId = response.body()?.memberId ?:""

            // JWT 토큰을 SharedPreferences에 저장
            sharedPreferences.edit().putString("jwt_token", accessToken).apply()
            sharedPreferences.edit().putString("refreshToken", refreshToken).apply()
            sharedPreferences.edit().putString("memberId", responseMemberId).apply()

            Log.d("LoginRepository", "jwt_token: $accessToken")
            Log.d("LoginRepository", "refreshToken: $refreshToken")
            Log.d("LoginRepository", "memberId: $responseMemberId")

            true
        } else {
            false
        }
    }
}