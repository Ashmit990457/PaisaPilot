package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import com.example.paisapilot.databinding.ActivitySavingsBinding;
import com.example.paisapilot.model.SavingsGoal;
import com.example.paisapilot.ui.adapters.SavingsAdapter;
import com.example.paisapilot.viewmodel.SavingsViewModel;
import com.google.android.material.snackbar.Snackbar;

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
            
            @Override
            public void onEditGoal(SavingsGoal goal) {
                editGoal(goal);
            }
        });
        binding.rvGoals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGoals.setAdapter(adapter);

        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        binding.rvGoals.addItemDecoration(new RecyclerView.ItemDecoration() {
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
                SavingsGoal goal = adapter.getGoals().get(position);
                deleteGoalWithUndo(goal, position);
            }
        }).attachToRecyclerView(binding.rvGoals);
    }

    private void editGoal(SavingsGoal goal) {
        Intent intent = new Intent(this, AddGoalActivity.class);
        intent.putExtra("edit_goal_id", goal.getGoalId());
        intent.putExtra("edit_title", goal.getTitle());
        intent.putExtra("edit_target", String.valueOf(goal.getTargetAmount()));
        intent.putExtra("edit_saved", String.valueOf(goal.getSavedAmount()));
        if (goal.getTargetDate() != null) {
            intent.putExtra("edit_date", goal.getTargetDate().toDate().getTime());
        }
        startActivityForResult(intent, ADD_GOAL_REQUEST);
    }

    private void showAddSavingsDialog(SavingsGoal goal) {
        EditText input = new EditText(this);
        input.setHint("Amount to add");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this)
                .setTitle("Add Savings: " + goal.getTitle())
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        viewModel.addSavings(goal.getGoalId(), val);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(SavingsGoal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete " + goal.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteGoal(goal.getGoalId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGoalWithUndo(SavingsGoal goal, int position) {
        viewModel.deleteGoal(goal.getGoalId());
        Snackbar.make(binding.rvGoals, "Goal deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    viewModel.undoDelete(goal.getGoalId());
                })
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
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        adapter.setGoals(resource.getData());
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
