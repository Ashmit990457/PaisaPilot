package com.example.paisapilot.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityAddGoalBinding;
import com.example.paisapilot.utils.AppConstants;
import com.example.paisapilot.viewmodel.SavingsViewModel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddGoalActivity extends BaseActivity {

    private ActivityAddGoalBinding binding;
    private SavingsViewModel viewModel;
    private Timestamp selectedDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private String goalIdToEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.addGoalRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(SavingsViewModel.class);

        setupGoalTypeSpinner();
        setupDatePicker();
        observeViewModel();
        prefillFromIntent();

        binding.btnSaveGoal.setOnClickListener(v -> onSaveGoalClicked());
    }

    private void prefillFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        goalIdToEdit = intent.getStringExtra("edit_goal_id");
        if (goalIdToEdit != null) {
            binding.toolbarAddGoal.setTitle("Edit Savings Goal");
            binding.btnSaveGoal.setText("Update Goal");
            binding.etGoalTitle.setText(intent.getStringExtra("edit_title"));
            binding.etTargetAmount.setText(intent.getStringExtra("edit_target"));
            binding.etSavedAmount.setText(intent.getStringExtra("edit_saved"));
            
            long dateLong = intent.getLongExtra("edit_date", -1);
            if (dateLong != -1) {
                Date date = new Date(dateLong);
                selectedDate = new Timestamp(date);
                binding.etTargetDate.setText(dateFormat.format(date));
            }
        }
    }

    private void setupGoalTypeSpinner() {
        String[] items = AppConstants.GOAL_TYPES.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        binding.spinnerGoalType.setAdapter(adapter);
    }

    private void setupDatePicker() {
        binding.etTargetDate.setOnClickListener(v -> openDatePicker());
        binding.etTargetDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) openDatePicker();
        });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 23, 59, 59);
            Date date = selected.getTime();
            selectedDate = new Timestamp(date);
            binding.etTargetDate.setText(dateFormat.format(date));
            binding.etTargetDate.clearFocus();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void observeViewModel() {
        viewModel.getGoalActionState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressSaveGoal.setVisibility(View.VISIBLE);
                    binding.btnSaveGoal.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressSaveGoal.setVisibility(View.GONE);
                    Toast.makeText(this, goalIdToEdit == null ? "Goal created" : "Goal updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    binding.progressSaveGoal.setVisibility(View.GONE);
                    binding.btnSaveGoal.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void onSaveGoalClicked() {
        String title = binding.etGoalTitle.getText().toString();
        String target = binding.etTargetAmount.getText().toString();
        String saved = binding.etSavedAmount.getText().toString();

        viewModel.saveGoal(goalIdToEdit, title, target, saved, selectedDate);
    }
}
