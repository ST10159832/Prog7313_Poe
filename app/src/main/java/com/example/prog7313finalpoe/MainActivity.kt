package com.example.prog7313finalpoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
//Jony Apps. 2021. Add Firebase to Android Project | Kotlin | Android Studio [Source Code]. Available at: < https://www.youtube.com/watch?v=K7QEyNVMxSA > [Accesssed on 28 May 2025]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle system window insets if needed
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize buttons
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val btnApple = findViewById<Button>(R.id.btnApple)
        val btnFacebook = findViewById<Button>(R.id.btnFacebook)
        val btnEmail = findViewById<Button>(R.id.btnEmail)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val goToSignUp = {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnGoogle.setOnClickListener { goToSignUp() }
        btnApple.setOnClickListener { goToSignUp() }
        btnFacebook.setOnClickListener { goToSignUp() }
        btnEmail.setOnClickListener { goToSignUp() }

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}