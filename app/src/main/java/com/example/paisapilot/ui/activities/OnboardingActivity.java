package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.paisapilot.databinding.ActivityOnboardingBinding;

/**
 * OnboardingActivity
 * 
 * Purpose: Placeholder onboarding screen that introduces the app to first-time users.
 * Contains a title, subtitle and a continue button. No navigation or onboarding logic
 * is implemented here.
 */
public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Intentionally no navigation or click behavior
    }
}
