package com.example.vision.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vision.R
import com.example.vision.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        var intent = getIntent()
        var url = intent.getStringExtra("url")

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.settings.javaScriptEnabled = true
        url?.let { binding.webView.loadUrl(it) }
    }
}