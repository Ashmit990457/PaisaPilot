package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityRegisterBinding;
import com.example.paisapilot.model.LoginResult;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.viewmodel.RegisterViewModel;
import com.example.paisapilot.ui.activities.ProfileSetupActivity;

/**
 * RegisterActivity
 * 
 * Purpose: Collects user information to create a new account. Includes fields for name,
 * email, password and confirm password. Validation and registration logic are omitted.
 */
public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Create ViewModel
        RegisterViewModel viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Observe registration state
        viewModel.getAuthState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.registerProgress.setVisibility(android.view.View.VISIBLE);
                    binding.btnRegister.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.registerProgress.setVisibility(android.view.View.GONE);
                    binding.btnRegister.setEnabled(true);
                    LoginResult data = resource.getData();
                    Toast.makeText(this, data != null ? data.getMessage() : "Registration successful", Toast.LENGTH_SHORT).show();
                    // Navigate to ProfileSetupActivity per requirements
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                    finish();
                    break;
                case ERROR:
                    binding.registerProgress.setVisibility(android.view.View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        // Handle register button click
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
            String confirm = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";
            viewModel.register(name, email, password, confirm);
        });
    }
}
