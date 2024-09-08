package com.sylovestp.firebasetest.testspringrestapp.repository

import android.content.SharedPreferences
import android.util.Log
import com.sylovestp.firebasetest.testspringrestapp.dto.LoginRequest
import com.sylovestp.firebasetest.testspringrestapp.dto.LoginResponse
import com.sylovestp.firebasetest.testspringrestapp.retrofit.INetworkService
import retrofit2.Response

class LoginRepository(private val apiService: INetworkService, private val sharedPreferences: SharedPreferences) {

    // 로그인 요청
    suspend fun login(memberId: String, memberPw: String): LoginResponse? {
        val loginRequest = LoginRequest(memberId, memberPw)
        val response = apiService.login(loginRequest)

        if (response.isSuccessful && response.body() != null) {
            val loginResponse = response.body()!!
            val accessToken = loginResponse.accessToken
            val refreshToken = loginResponse.refreshToken
            val responseMemberId = loginResponse.memberId
            Log.d("lsy accessToken","accessToken : ${accessToken}" )
            Log.d("lsy refreshToken","refreshToken : ${refreshToken}" )
            Log.d("lsy responseMemberId","responseMemberId : ${responseMemberId}")

            // JWT 토큰을 SharedPreferences에 저장
            sharedPreferences.edit().apply {
                putString("jwt_token", accessToken)
                putString("refreshToken", refreshToken)
                putString("memberId", responseMemberId)
                apply()
            }

            // 데이터 저장 후 확인 (디버깅용)
            val savedMemberId = sharedPreferences.getString("memberId", "123456")
            Log.d("LoginRepository", "저장된 회원 번호: $savedMemberId")
            Log.d("LoginRepository", "응답 회원 번호: $responseMemberId")

//            if (savedMemberNo != responseMemberNo) {
//                Log.e("LoginRepository", "저장된 memberNo가 예상 값과 일치하지 않음")
//                throw RuntimeException("Saved memberNo does not match expected value")
//            }

            return loginResponse
        } else {
            Log.e("LoginRepository", "API 호출 실패: ${response.code()} ${response.message()}")
            return null
        }
    }

    // 회원 삭제 요청
    suspend fun deleteUser(token: String, memberId: String): Response<Unit > {
        return apiService.deleteUser(token, memberId)
    }

    // JWT 토큰 가져오기
    fun getJwtToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }
}
