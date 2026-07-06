package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.ViewModelProvider;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.paisapilot.databinding.ActivityLoginBinding;
import com.example.paisapilot.model.LoginResult;
import com.example.paisapilot.viewmodel.LoginViewModel;
import com.example.paisapilot.ui.activities.MainActivity;

/**
 * LoginActivity
 * 
 * Purpose: Presents authentication fields for an existing user. Contains email and
 * password inputs and actions to log in or navigate to registration. Click handling
 * and authentication logic are intentionally omitted.
 */
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Create ViewModel using the default ViewModelProvider for simplicity
        LoginViewModel viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observe authentication state which carries loading, success and error states
        viewModel.getAuthState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnLogin.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    LoginResult data = resource.getData();
                    if (data != null && data.isSuccess()) {
                        Toast.makeText(LoginActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                        // Go to Splash to perform initial sync/check
                        Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Shouldn't normally happen: success state without data
                        Toast.makeText(LoginActivity.this, "Login succeeded", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // On click, delegate to ViewModel which validates and performs login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            viewModel.login(email, password);
        });

        // Navigate to RegisterActivity when the register TextView is clicked.
        // Uses the TextView with id tv_register defined in activity_login.xml.
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
