package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.paisapilot.databinding.ActivityAllExpensesBinding;
import com.example.paisapilot.ui.adapters.ExpenseAdapter;
import com.example.paisapilot.viewmodel.ExpenseViewModel;

public class AllExpensesActivity extends BaseActivity {

    private ActivityAllExpensesBinding binding;
    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAllExpensesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.allExpensesRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarAllExpenses);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarAllExpenses.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        setupRecyclerView();
        observeViewModel();

        viewModel.loadExpenses();
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter();
        adapter.setOnExpenseLongClickListener(expense -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteExpense(expense.getExpenseId());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.rvAllExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllExpenses.setAdapter(adapter);

        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvAllExpenses.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull androidx.recyclerview.widget.RecyclerView parent, @NonNull androidx.recyclerview.widget.RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
    }

    private void observeViewModel() {
        viewModel.getExpensesState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressAllExpenses.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressAllExpenses.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        adapter.setExpenses(resource.getData());
                    }
                    break;
                case ERROR:
                    binding.progressAllExpenses.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getDeleteExpenseState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressAllExpenses.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressAllExpenses.setVisibility(View.GONE);
                    Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                    viewModel.loadExpenses();
                    break;
                case ERROR:
                    binding.progressAllExpenses.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
