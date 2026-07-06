package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.paisapilot.databinding.ActivityRecurringBinding;
import com.example.paisapilot.model.RecurringExpense;
import com.example.paisapilot.ui.adapters.RecurringAdapter;
import com.example.paisapilot.viewmodel.RecurringViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RecurringExpensesActivity extends BaseActivity {

    private ActivityRecurringBinding binding;
    private RecurringViewModel viewModel;
    private RecurringAdapter adapter;
    private static final int ADD_RECURRING_REQUEST = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRecurringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.recurringRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarRecurring);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarRecurring.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(RecurringViewModel.class);
        setupRecyclerView();
        observeViewModel();

        binding.fabAddRecurring.setOnClickListener(v -> 
                startActivityForResult(new Intent(this, AddRecurringExpenseActivity.class), ADD_RECURRING_REQUEST)
        );
    }

    private void setupRecyclerView() {
        adapter = new RecurringAdapter();
        adapter.setOnRecurringInteractionListener(new RecurringAdapter.OnRecurringInteractionListener() {
            @Override
            public void onDelete(RecurringExpense recurring) {
                showDeleteConfirmation(recurring);
            }

            @Override
            public void onEdit(RecurringExpense recurring) {
                editRecurring(recurring);
            }

            @Override
            public void onMarkPaid(RecurringExpense recurring) {
                viewModel.markPaid(recurring);
            }
        });
        binding.rvRecurring.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecurring.setAdapter(adapter);

        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvRecurring.addItemDecoration(new RecyclerView.ItemDecoration() {
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
                RecurringExpense item = adapter.getList().get(position);
                deleteRecurringWithUndo(item, position);
            }
        }).attachToRecyclerView(binding.rvRecurring);
    }

    private void editRecurring(RecurringExpense item) {
        Intent intent = new Intent(this, AddRecurringExpenseActivity.class);
        intent.putExtra("edit_recurring_id", item.getId());
        intent.putExtra("edit_title", item.getTitle());
        intent.putExtra("edit_amount", String.valueOf(item.getAmount()));
        intent.putExtra("edit_frequency", item.getFrequency().name());
        intent.putExtra("edit_reminder", item.isReminderEnabled());
        intent.putExtra("edit_auto_add", item.isAutoAddExpense());
        if (item.getNextDueDate() != null) {
            intent.putExtra("edit_date", item.getNextDueDate().toDate().getTime());
        }
        startActivityForResult(intent, ADD_RECURRING_REQUEST);
    }

    private void showDeleteConfirmation(RecurringExpense item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recurring Bill")
                .setMessage("Are you sure you want to delete " + item.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteRecurring(item.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRecurringWithUndo(RecurringExpense item, int position) {
        viewModel.deleteRecurring(item.getId());
        Snackbar.make(binding.rvRecurring, "Recurring bill deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    viewModel.undoDelete(item.getId());
                })
                .show();
    }

    private void observeViewModel() {
        viewModel.getRecurringListState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressRecurring.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressRecurring.setVisibility(View.GONE);
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        adapter.setList(resource.getData());
                        binding.rvRecurring.setVisibility(View.VISIBLE);
                        binding.emptyStateRecurring.setVisibility(View.GONE);
                    } else {
                        binding.rvRecurring.setVisibility(View.GONE);
                        binding.emptyStateRecurring.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressRecurring.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getRecurringActionState().observe(this, resource -> {
            if (resource == null) return;
            if (resource.getStatus() == com.example.paisapilot.model.Resource.Status.ERROR) {
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
