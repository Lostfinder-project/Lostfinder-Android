package com.example.lostfinder.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.lostfinder.R
import com.example.lostfinder.ui.post.list.PostListActivity
import com.example.lostfinder.ui.signup.SignupActivity
import kotlinx.coroutines.flow.collectLatest

class LoginActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val editId = findViewById<EditText>(R.id.editId)
        val editPw = findViewById<EditText>(R.id.editPw)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)
        val btnGoSignup = findViewById<Button>(R.id.btnGoSignup)

        btnLogin.setOnClickListener {
            val id = editId.text.toString()
            val pw = editPw.text.toString()

            viewModel.login(id, pw)
        }


        btnGoSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginViewModel.LoginState.Loading -> progress.visibility = ProgressBar.VISIBLE
                    is LoginViewModel.LoginState.Success -> {
                        progress.visibility = ProgressBar.GONE
                        startActivity(Intent(this@LoginActivity, PostListActivity::class.java))
                        finish()
                    }
                    is LoginViewModel.LoginState.Error -> {
                        progress.visibility = ProgressBar.GONE
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> progress.visibility = ProgressBar.GONE
                }
            }
        }
    }
}
