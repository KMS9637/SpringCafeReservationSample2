package com.sylovestp.firebasetest.testspringrestapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityMainBinding
import com.sylovestp.firebasetest.testspringrestapp.databinding.ActivityUserRecyclerViewBinding

class UserRecyclerViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityUserRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fragment를 추가할 컨테이너가 존재하는지 확인 (예: R.id.fragment_container)
        if (savedInstanceState == null) {
            val fragment = ItemFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)  // R.id.fragment_container는 activity_main.xml에 정의된 컨테이너의 ID입니다.
                .commit()
        }

    }//onCreate

}