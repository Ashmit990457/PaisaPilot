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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ActivityBudgetBinding;
import com.example.paisapilot.model.Budget;
import com.example.paisapilot.ui.adapters.BudgetAdapter;
import com.example.paisapilot.viewmodel.BudgetViewModel;
import com.google.android.material.snackbar.Snackbar;

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
        setupSearch();
        observeViewModel();

        binding.fabAddBudget.setOnClickListener(v -> 
                startActivityForResult(new Intent(this, AddBudgetActivity.class), ADD_BUDGET_REQUEST)
        );
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter();
        adapter.setOnBudgetLongClickListener(this::showBudgetPopupMenu);
        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBudgets.setAdapter(adapter);
        
        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvBudgets.addItemDecoration(new RecyclerView.ItemDecoration() {
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
                Budget budget = adapter.getBudgets().get(position);
                deleteBudgetWithUndo(budget);
            }
        }).attachToRecyclerView(binding.rvBudgets);
    }

    private void setupSearch() {
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
    }

    private void showBudgetPopupMenu(Budget budget) {
        RecyclerView.ViewHolder holder = binding.rvBudgets.findViewHolderForAdapterPosition(adapter.getBudgets().indexOf(budget));
        if (holder == null) return;

        PopupMenu popup = new PopupMenu(this, holder.itemView);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) {
                editBudget(budget);
            } else {
                showDeleteConfirmation(budget);
            }
            return true;
        });
        popup.show();
    }

    private void editBudget(Budget budget) {
        Intent intent = new Intent(this, AddBudgetActivity.class);
        intent.putExtra("edit_budget_id", budget.getBudgetId());
        intent.putExtra("edit_category", budget.getCategory());
        intent.putExtra("edit_limit", String.valueOf(budget.getMonthlyLimit()));
        intent.putExtra("edit_spent", budget.getSpentAmount());
        startActivityForResult(intent, ADD_BUDGET_REQUEST);
    }

    private void showDeleteConfirmation(Budget budget) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete the budget for " + budget.getCategory() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteBudgetWithUndo(budget);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBudgetWithUndo(Budget budget) {
        viewModel.deleteBudget(budget.getBudgetId());
        Snackbar.make(binding.budgetRoot, "Budget deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> viewModel.undoDelete(budget.getBudgetId()))
                .show();
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
                    if (resource.getData() != null) {
                        adapter.setBudgets(resource.getData());
                        boolean isEmpty = resource.getData().isEmpty();
                        binding.rvBudgets.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                        binding.emptyStateBudget.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    }
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
    }
}
