package com.sylovestp.firebasetest.testspringrestapp.retrofit

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application(){

    // http 퍼미션 허용 및, 로컬호스트 안될시 아이피로 확인 하기.
    val BASE_URL = "http://192.168.219.200:8080"

    //add....................................
    var networkService: INetworkService

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    init {
        networkService = retrofit.create(INetworkService::class.java)

    }
}