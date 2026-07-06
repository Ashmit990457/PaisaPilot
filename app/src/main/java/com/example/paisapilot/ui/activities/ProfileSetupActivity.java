package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.databinding.ActivityProfileSetupBinding;
import com.example.paisapilot.model.UserProfile;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.LoginResult;
import com.example.paisapilot.viewmodel.ProfileSetupViewModel;

/**
 * ProfileSetupActivity
 *
 * Purpose: Collects user profile information (name, city, income, saving goal, etc.)
 * and saves it to Firestore via ProfileSetupViewModel. Observes the Resource<UserProfile>
 * LiveData to show loading, success and error states.
 */
public class ProfileSetupActivity extends AppCompatActivity {
    private ActivityProfileSetupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityProfileSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        ProfileSetupViewModel viewModel = new ViewModelProvider(this).get(ProfileSetupViewModel.class);

        // Set session if user is already authenticated (from RegisterActivity)
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SessionManager.getInstance(this).setLogin(user.getUid());
        }

        // Observe profile save state
        viewModel.getState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.profileProgress.setVisibility(android.view.View.VISIBLE);
                    binding.btnSaveProfile.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.profileProgress.setVisibility(android.view.View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                    // Navigate to MainActivity
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                case ERROR:
                    binding.profileProgress.setVisibility(android.view.View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        binding.btnSaveProfile.setOnClickListener(v -> {
            String name = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
            String occupation = binding.etOccupation.getText() != null ? binding.etOccupation.getText().toString().trim() : "";
            String city = binding.etCity.getText() != null ? binding.etCity.getText().toString().trim() : "";
            String currency = binding.etCurrency.getText() != null ? binding.etCurrency.getText().toString().trim() : "";
            double income = 0d;
            double saving = 0d;
            int salaryDate = 1;
            try {
                String inc = binding.etIncome.getText() != null ? binding.etIncome.getText().toString().trim() : "";
                if (!inc.isEmpty()) income = Double.parseDouble(inc);
            } catch (NumberFormatException ignored) {}
            try {
                String s = binding.etSavingGoal.getText() != null ? binding.etSavingGoal.getText().toString().trim() : "";
                if (!s.isEmpty()) saving = Double.parseDouble(s);
            } catch (NumberFormatException ignored) {}
            try {
                String d = binding.etSalaryDate.getText() != null ? binding.etSalaryDate.getText().toString().trim() : "";
                if (!d.isEmpty()) salaryDate = Integer.parseInt(d);
            } catch (NumberFormatException ignored) {}

            // Validate inputs via ViewModel
            com.example.paisapilot.model.ValidationResult validation = viewModel.validateInputs(name, occupation, city, income, saving, currency, salaryDate);
            if (!validation.isSuccess()) {
                Toast.makeText(this, validation.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfile profile = new UserProfile(name, occupation, city, income, saving, currency.isEmpty() ? "INR" : currency, salaryDate);
            viewModel.saveProfile(profile);
        });
    }
}
