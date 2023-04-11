package com.yeocak.wordpuzzle.ui.screen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.yeocak.wordpuzzle.data.local.DBHelper
import com.yeocak.wordpuzzle.databinding.ActivityMainBinding
import com.yeocak.wordpuzzle.model.Score

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // KODUNU BURAYA YAZ
    }
}