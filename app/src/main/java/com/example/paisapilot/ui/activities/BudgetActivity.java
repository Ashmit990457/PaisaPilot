package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.paisapilot.databinding.ActivityBudgetBinding;
import com.example.paisapilot.ui.adapters.BudgetAdapter;
import com.example.paisapilot.viewmodel.BudgetViewModel;

public class BudgetActivity extends BaseActivity {

    private ActivityBudgetBinding binding;
    private BudgetViewModel viewModel;
    private BudgetAdapter adapter;
    private static final int ADD_BUDGET_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.budgetRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarBudget);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarBudget.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        setupRecyclerView();
        observeViewModel();

        binding.fabAddBudget.setOnClickListener(v -> 
                startActivityForResult(new Intent(this, AddBudgetActivity.class), ADD_BUDGET_REQUEST)
        );

        viewModel.loadBudgets();
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter();
        adapter.setOnBudgetLongClickListener(budget -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Budget")
                    .setMessage("Are you sure you want to delete the budget for " + budget.getCategory() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteBudget(budget.getBudgetId());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBudgets.setAdapter(adapter);
        
        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvBudgets.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull androidx.recyclerview.widget.RecyclerView parent, @NonNull androidx.recyclerview.widget.RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
    }

    private void observeViewModel() {
        viewModel.getBudgetsState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBudget.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBudget.setVisibility(View.GONE);
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        adapter.setBudgets(resource.getData());
                        binding.rvBudgets.setVisibility(View.VISIBLE);
                        binding.emptyStateBudget.setVisibility(View.GONE);
                    } else {
                        binding.rvBudgets.setVisibility(View.GONE);
                        binding.emptyStateBudget.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBudget.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getDeleteBudgetState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBudget.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBudget.setVisibility(View.GONE);
                    Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show();
                    viewModel.loadBudgets();
                    break;
                case ERROR:
                    binding.progressBudget.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_BUDGET_REQUEST && resultCode == RESULT_OK) {
            setResult(RESULT_OK); // Inform MainActivity that data changed
            viewModel.loadBudgets();
        }
    }
}
