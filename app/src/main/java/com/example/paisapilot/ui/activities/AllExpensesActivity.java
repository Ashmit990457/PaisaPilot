package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.PopupMenu;
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

import com.example.paisapilot.R;
import com.example.paisapilot.databinding.ActivityAllExpensesBinding;
import com.example.paisapilot.databinding.BottomSheetFilterExpensesBinding;
import com.example.paisapilot.model.Expense;
import com.example.paisapilot.ui.adapters.ExpenseAdapter;
import com.example.paisapilot.viewmodel.ExpenseViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class AllExpensesActivity extends BaseActivity {

    private ActivityAllExpensesBinding binding;
    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;

    // Filter states
    private String selectedDateFilter = null;
    private final List<String> selectedPayments = new ArrayList<>();
    private String selectedSort = "Newest First";

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
        setupSearchAndFilters();
        observeViewModel();
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

        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvAllExpenses.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });

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

    private void setupSearchAndFilters() {
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipFilter.setOnClickListener(v -> showFilterBottomSheet());
        binding.chipSort.setOnClickListener(v -> showSortPopup());
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetFilterExpensesBinding filterBinding = BottomSheetFilterExpensesBinding.inflate(getLayoutInflater());
        dialog.setContentView(filterBinding.getRoot());

        // Restore state
        if (selectedDateFilter != null) {
            switch (selectedDateFilter) {
                case "Today": filterBinding.chipToday.setChecked(true); break;
                case "Yesterday": filterBinding.chipYesterday.setChecked(true); break;
                case "Last 7 Days": filterBinding.chipLast7Days.setChecked(true); break;
                case "This Month": filterBinding.chipThisMonth.setChecked(true); break;
            }
        }
        for (String p : selectedPayments) {
            if (p.equals("Cash")) filterBinding.chipPayCash.setChecked(true);
            if (p.equals("UPI")) filterBinding.chipPayUpi.setChecked(true);
            if (p.equals("Card")) filterBinding.chipPayCard.setChecked(true);
        }

        filterBinding.btnApplyFilters.setOnClickListener(v -> {
            int checkedId = filterBinding.cgDateFilters.getCheckedChipId();
            if (checkedId == R.id.chip_today) selectedDateFilter = "Today";
            else if (checkedId == R.id.chip_yesterday) selectedDateFilter = "Yesterday";
            else if (checkedId == R.id.chip_last_7_days) selectedDateFilter = "Last 7 Days";
            else if (checkedId == R.id.chip_this_month) selectedDateFilter = "This Month";
            else selectedDateFilter = null;

            selectedPayments.clear();
            if (filterBinding.chipPayCash.isChecked()) selectedPayments.add("Cash");
            if (filterBinding.chipPayUpi.isChecked()) selectedPayments.add("UPI");
            if (filterBinding.chipPayCard.isChecked()) selectedPayments.add("Card");

            viewModel.setFilters(selectedDateFilter, selectedPayments, selectedSort);
            dialog.dismiss();
        });

        filterBinding.btnClearFilters.setOnClickListener(v -> {
            selectedDateFilter = null;
            selectedPayments.clear();
            viewModel.clearFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSortPopup() {
        PopupMenu popup = new PopupMenu(this, binding.chipSort);
        popup.getMenu().add("Newest First");
        popup.getMenu().add("Oldest First");
        popup.getMenu().add("Highest Amount");
        popup.getMenu().add("Lowest Amount");
        popup.getMenu().add("A-Z");
        popup.getMenu().add("Z-A");

        popup.setOnMenuItemClickListener(item -> {
            selectedSort = item.getTitle().toString();
            viewModel.setFilters(selectedDateFilter, selectedPayments, selectedSort);
            return true;
        });
        popup.show();
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
        Snackbar.make(binding.allExpensesRoot, "Expense deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> viewModel.undoDelete(expense.getExpenseId()))
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
                        boolean isEmpty = resource.getData().isEmpty();
                        binding.rvAllExpenses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                        binding.layoutEmptySearch.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
