package com.example.paisapilot.ui.activities;

import android.content.Intent;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ActivityAllExpensesBinding;
import com.example.paisapilot.model.Expense;
import com.example.paisapilot.ui.adapters.ExpenseAdapter;
import com.example.paisapilot.viewmodel.ExpenseViewModel;
import com.google.android.material.snackbar.Snackbar;

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
        adapter.setOnExpenseInteractionListener(new ExpenseAdapter.OnExpenseInteractionListener() {
            @Override
            public void onEditExpense(Expense expense) {
                editExpense(expense);
            }

            @Override
            public void onDeleteExpense(Expense expense) {
                showDeleteConfirmation(expense);
            }
        });
        binding.rvAllExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllExpenses.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expense = adapter.getExpenses().get(position);
                deleteExpenseWithUndo(expense);
            }
        }).attachToRecyclerView(binding.rvAllExpenses);
    }

    private void editExpense(Expense expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("edit_expense_id", expense.getExpenseId());
        intent.putExtra("edit_title", expense.getTitle());
        intent.putExtra("edit_amount", String.valueOf(expense.getAmount()));
        intent.putExtra("edit_category", expense.getCategory());
        intent.putExtra("edit_payment", expense.getPaymentMethod());
        intent.putExtra("edit_note", expense.getNote());
        intent.putExtra("edit_user_id", expense.getUserId());
        if (expense.getDate() != null) {
            intent.putExtra("edit_date", expense.getDate().toDate().getTime());
        }
        if (expense.getCreatedAt() != null) {
            intent.putExtra("edit_created_at", expense.getCreatedAt().toDate().getTime());
        }
        startActivity(intent);
    }

    private void showDeleteConfirmation(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteExpenseWithUndo(expense);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpenseWithUndo(Expense expense) {
        viewModel.deleteExpense(expense.getExpenseId());
        
        Snackbar.make(binding.rvAllExpenses, "Expense deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    viewModel.undoDelete(expense.getExpenseId());
                })
                .show();
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
    }
}
