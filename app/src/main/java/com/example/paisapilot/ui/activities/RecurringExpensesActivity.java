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

import com.example.paisapilot.databinding.ActivityRecurringBinding;
import com.example.paisapilot.model.RecurringExpense;
import com.example.paisapilot.ui.adapters.RecurringAdapter;
import com.example.paisapilot.viewmodel.RecurringViewModel;

import java.util.List;

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
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
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

        viewModel.loadRecurringExpenses();
    }

    private void setupRecyclerView() {
        adapter = new RecurringAdapter();
        adapter.setOnRecurringInteractionListener(recurring -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Recurring Bill")
                    .setMessage("Are you sure you want to delete " + recurring.getTitle() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteRecurring(recurring.getId());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.rvRecurring.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecurring.setAdapter(adapter);

        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvRecurring.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull androidx.recyclerview.widget.RecyclerView parent, @NonNull androidx.recyclerview.widget.RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
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
                    List<RecurringExpense> data = resource.getData();
                    if (data != null && !data.isEmpty()) {
                        adapter.setList(data);
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
            if (resource.getStatus() == com.example.paisapilot.model.Resource.Status.SUCCESS) {
                viewModel.loadRecurringExpenses();
            } else if (resource.getStatus() == com.example.paisapilot.model.Resource.Status.ERROR) {
                Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_RECURRING_REQUEST && resultCode == RESULT_OK) {
            viewModel.loadRecurringExpenses();
        }
    }
}
