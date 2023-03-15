package com.azamovhudstc.playstoretodoapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import com.azamovhudstc.playstoretodoapp.fragments.HomeFragment
import com.azamovhudstc.playstoretodoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (intent.getStringExtra("noti") != null) {
            val bundle = bundleOf(Pair("noti", intent.getStringExtra("noti")))
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment::class.java,
                bundle
            ).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
    }
}