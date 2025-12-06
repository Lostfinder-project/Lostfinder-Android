// com.example.lostfinder.ui.signup.SignupActivity
package com.example.lostfinder.ui.signup

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.lostfinder.R
import kotlinx.coroutines.flow.collectLatest

class SignupActivity : ComponentActivity() {

    private lateinit var viewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]

        val editUsername = findViewById<EditText>(R.id.editUsername)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editNickname = findViewById<EditText>(R.id.editNickname)
        val editPhone = findViewById<EditText>(R.id.editPhone)
        val editEmail = findViewById<EditText>(R.id.editEmail)   // 🔥 추가
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val progressBar = findViewById<ProgressBar>(R.id.progressSignup)

        btnSignup.setOnClickListener {
            viewModel.signup(
                editUsername.text.toString(),
                editPassword.text.toString(),
                editNickname.text.toString(),
                editPhone.text.toString(),
                editEmail.text.toString()
            )
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is SignupViewModel.SignupState.Loading ->
                        progressBar.visibility = ProgressBar.VISIBLE

                    is SignupViewModel.SignupState.Success -> {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@SignupActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    is SignupViewModel.SignupState.Error -> {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@SignupActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }

                    else -> progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }
}
