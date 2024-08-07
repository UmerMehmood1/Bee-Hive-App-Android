package com.umer.beehiveclient.activities

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.customViews.WaveView
import com.umer.beehiveclient.databinding.ActivityLoginBinding
import com.umer.beehiveclient.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var driveService: Drive

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
            override fun OnWaveAnimation(y: Float) {}
        })
        setListeners()
    }

    private fun setListeners() {
        initGoogleSignIn()
        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInResultLauncher.launch(signInIntent)
    }

    private val signInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                // Save user details
                val email = account.email
                val name = account.displayName
                val picUrl = account.photoUrl
                Util.saveUser(
                    this,
                    User(
                        name.toString(),
                        email.toString(),
                        picUrl.toString(),
                        account.id.toString(),
                        null
                    )
                )
                // Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } catch (e: ApiException) {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Sign-in failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}
