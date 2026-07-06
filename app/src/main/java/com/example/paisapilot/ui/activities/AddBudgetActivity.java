package com.example.paisapilot.ui.activities;

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

import com.example.paisapilot.databinding.ActivityAddBudgetBinding;
import com.example.paisapilot.utils.AppConstants;
import com.example.paisapilot.viewmodel.BudgetViewModel;

public class AddBudgetActivity extends BaseActivity {

    private ActivityAddBudgetBinding binding;
    private BudgetViewModel viewModel;
    private boolean isSaving = false;
    private String budgetIdToEdit = null;
    private double currentSpent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.addBudgetRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        setupCategorySpinner();
        observeViewModel();
        prefillFromIntent();

        binding.btnSaveBudget.setOnClickListener(v -> onSaveBudgetClicked());
    }

    private void prefillFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;
        
        budgetIdToEdit = intent.getStringExtra("edit_budget_id");
        if (budgetIdToEdit != null) {
            binding.toolbarAddBudget.setTitle("Edit Budget");
            binding.btnSaveBudget.setText("Update Budget");
            binding.spinnerBudgetCategory.setText(intent.getStringExtra("edit_category"), false);
            binding.etBudgetLimit.setText(intent.getStringExtra("edit_limit"));
            currentSpent = intent.getDoubleExtra("edit_spent", 0);
        }
    }

    private void setupCategorySpinner() {
        String[] items = AppConstants.EXPENSE_CATEGORIES.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        binding.spinnerBudgetCategory.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getCreateBudgetState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressSaveBudget.setVisibility(View.VISIBLE);
                    binding.btnSaveBudget.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressSaveBudget.setVisibility(View.GONE);
                    Toast.makeText(this, budgetIdToEdit == null ? "Budget saved" : "Budget updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    isSaving = false;
                    binding.progressSaveBudget.setVisibility(View.GONE);
                    binding.btnSaveBudget.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void onSaveBudgetClicked() {
        if (isSaving) return;

        String category = binding.spinnerBudgetCategory.getText().toString();
        String limit = binding.etBudgetLimit.getText() != null ? binding.etBudgetLimit.getText().toString() : "";

        isSaving = true;
        binding.btnSaveBudget.setEnabled(false);
        binding.progressSaveBudget.setVisibility(View.VISIBLE);

        viewModel.saveBudget(budgetIdToEdit, category, limit, currentSpent);
    }
}
