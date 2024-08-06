package com.umer.beehiveclient.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.customViews.WaveView
import com.umer.beehiveclient.databinding.ActivityLoginBinding
import com.umer.beehiveclient.models.User


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Util.getUser(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.waveView.setOnWaveAnimationListener(object : WaveView.OnWaveAnimationListener {
            override fun OnWaveAnimation(y: Float) {
            }
        })
        setListeners()
    }

    private fun setListeners() {
        initGoogleSignIn()
        binding.signInButton.setOnClickListener {
            signInWithGoogle(googleSignInClient)
        }
    }
    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    private fun signInWithGoogle(googleSignInClient: GoogleSignInClient) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1999999)
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1999999) {
            val task: Task<GoogleSignInAccount?> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount?>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account!=null){
                val email   = account.email
                val name    = account.displayName
                val picUrl  = account.photoUrl
                Util.saveUser(this, User(name.toString(),email.toString(),picUrl.toString(),account.id.toString(), null))
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            runOnUiThread {
                if (e.message?.contains("7") == true){
                    Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT)
                        .show()
                }else{
                    Toast.makeText(applicationContext, "Something went wrong!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}