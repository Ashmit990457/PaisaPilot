package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.databinding.ActivitySettingsBinding;
import com.example.paisapilot.viewmodel.SettingsViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarSettings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarSettings.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupClickListeners();
        updateUI();
    }

    private void setupClickListeners() {
        binding.btnViewProfile.setOnClickListener(v -> 
                startActivity(new Intent(this, ProfileSetupActivity.class)));
        
        binding.btnEditProfile.setOnClickListener(v -> 
                startActivity(new Intent(this, ProfileSetupActivity.class)));

        binding.layoutTheme.setOnClickListener(v -> showThemeDialog());
        binding.layoutCurrency.setOnClickListener(v -> showCurrencyDialog());
        
        binding.btnNotificationSettings.setOnClickListener(v -> 
                startActivity(new Intent(this, NotificationSettingsActivity.class)));

        binding.btnExportPDF.setOnClickListener(v -> Toast.makeText(this, "Exporting PDF...", Toast.LENGTH_SHORT).show());
        binding.btnExportCSV.setOnClickListener(v -> Toast.makeText(this, "Exporting CSV...", Toast.LENGTH_SHORT).show());

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    private void updateUI() {
        int themeMode = viewModel.getThemeMode();
        String themeText = "System Default";
        if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) themeText = "Dark";
        else if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) themeText = "Light";
        binding.tvCurrentTheme.setText(themeText);

        binding.tvCurrentCurrency.setText(viewModel.getCurrency());
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        int current = 2;
        int mode = viewModel.getThemeMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) current = 0;
        else if (mode == AppCompatDelegate.MODE_NIGHT_YES) current = 1;

        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themes, current, (dialog, which) -> {
                    int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    if (which == 0) selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
                    else if (which == 1) selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                    
                    viewModel.setThemeMode(selectedMode);
                    updateUI();
                    dialog.dismiss();
                })
                .show();
    }

    private void showCurrencyDialog() {
        String[] currencies = {"INR (₹)", "USD ($)", "EUR (€)", "GBP (£)"};
        new AlertDialog.Builder(this)
                .setTitle("Select Currency")
                .setItems(currencies, (dialog, which) -> {
                    String code = currencies[which].split(" ")[0];
                    viewModel.setCurrency(code);
                    updateUI();
                })
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();
        
        new Thread(() -> {
            AppDatabase.getInstance(this).clearAllTables();
            runOnUiThread(() -> {
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            });
        }).start();
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This action is permanent and will delete all your data. Continue?")
                .setPositiveButton("Delete Permanently", (dialog, which) -> {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
