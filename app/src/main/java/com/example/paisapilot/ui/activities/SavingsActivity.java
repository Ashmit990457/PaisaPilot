package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
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

import com.example.paisapilot.databinding.ActivitySavingsBinding;
import com.example.paisapilot.model.SavingsGoal;
import com.example.paisapilot.ui.adapters.SavingsAdapter;
import com.example.paisapilot.viewmodel.SavingsViewModel;

import java.util.List;

public class SavingsActivity extends BaseActivity {

    private ActivitySavingsBinding binding;
    private SavingsViewModel viewModel;
    private SavingsAdapter adapter;
    private static final int ADD_GOAL_REQUEST = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySavingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.savingsRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarSavings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarSavings.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(SavingsViewModel.class);
        setupRecyclerView();
        observeViewModel();

        binding.fabAddGoal.setOnClickListener(v -> 
                startActivityForResult(new Intent(this, AddGoalActivity.class), ADD_GOAL_REQUEST)
        );

        viewModel.loadGoals();
    }

    private void setupRecyclerView() {
        adapter = new SavingsAdapter();
        adapter.setOnGoalInteractionListener(new SavingsAdapter.OnGoalInteractionListener() {
            @Override
            public void onAddSavings(SavingsGoal goal) {
                showAddSavingsDialog(goal);
            }

            @Override
            public void onDeleteGoal(SavingsGoal goal) {
                showDeleteConfirmation(goal);
            }
        });
        binding.rvGoals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGoals.setAdapter(adapter);

        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvGoals.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull androidx.recyclerview.widget.RecyclerView parent, @NonNull androidx.recyclerview.widget.RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
    }

    private void showAddSavingsDialog(SavingsGoal goal) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount to save");

        new AlertDialog.Builder(this)
                .setTitle("Add Savings to " + goal.getTitle())
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String amount = input.getText().toString();
                    viewModel.addSavings(goal.getGoalId(), amount);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(SavingsGoal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete the goal: " + goal.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteGoal(goal.getGoalId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getGoalsState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressSavings.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressSavings.setVisibility(View.GONE);
                    List<SavingsGoal> goals = resource.getData();
                    if (goals != null && !goals.isEmpty()) {
                        adapter.setGoals(goals);
                        binding.rvGoals.setVisibility(View.VISIBLE);
                        binding.emptyStateGoals.setVisibility(View.GONE);
                    } else {
                        binding.rvGoals.setVisibility(View.GONE);
                        binding.emptyStateGoals.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressSavings.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getGoalActionState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressSavings.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressSavings.setVisibility(View.GONE);
                    viewModel.loadGoals(); // Refresh
                    break;
                case ERROR:
                    binding.progressSavings.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_GOAL_REQUEST && resultCode == RESULT_OK) {
            viewModel.loadGoals();
        }
    }
}
