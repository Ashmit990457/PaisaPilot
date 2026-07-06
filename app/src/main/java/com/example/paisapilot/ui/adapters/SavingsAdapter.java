package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemGoalBinding;
import com.example.paisapilot.model.SavingsGoal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.SavingsViewHolder> {

    private final List<SavingsGoal> goals = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnGoalInteractionListener listener;

    public interface OnGoalInteractionListener {
        void onAddSavings(SavingsGoal goal);
        void onDeleteGoal(SavingsGoal goal);
    }

    public void setOnGoalInteractionListener(OnGoalInteractionListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<SavingsGoal> newGoals) {
        goals.clear();
        if (newGoals != null) {
            goals.addAll(newGoals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoalBinding binding = ItemGoalBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SavingsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsViewHolder holder, int position) {
        holder.bind(goals.get(position));
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    class SavingsViewHolder extends RecyclerView.ViewHolder {
        private final ItemGoalBinding binding;

        public SavingsViewHolder(@NonNull ItemGoalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SavingsGoal goal) {
            binding.tvGoalTitle.setText(goal.getTitle());
            binding.tvGoalPercentage.setText(String.format(Locale.getDefault(), "%d%%", goal.getPercentage()));
            binding.progressGoal.setProgress(goal.getPercentage());
            
            binding.tvSavedInfo.setText(String.format(Locale.getDefault(), "Saved: ₹%.2f", goal.getSavedAmount()));
            binding.tvTargetInfo.setText(String.format(Locale.getDefault(), "Target: ₹%.2f", goal.getTargetAmount()));
            binding.tvRemainingInfo.setText(String.format(Locale.getDefault(), "Remaining: ₹%.2f", goal.getRemainingAmount()));

            if (goal.getTargetDate() != null) {
                binding.tvTargetDateInfo.setText(String.format(Locale.getDefault(), "By: %s", dateFormat.format(goal.getTargetDate().toDate())));
            } else {
                binding.tvTargetDateInfo.setText("");
            }

            if (goal.isCompleted()) {
                binding.tvCompletedTag.setVisibility(View.VISIBLE);
                binding.btnAddSavings.setEnabled(false);
            } else {
                binding.tvCompletedTag.setVisibility(View.GONE);
                binding.btnAddSavings.setEnabled(true);
            }

            binding.btnAddSavings.setOnClickListener(v -> {
                if (listener != null) listener.onAddSavings(goal);
            });

            binding.btnDeleteGoal.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteGoal(goal);
            });
        }
    }
}
