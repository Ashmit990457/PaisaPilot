package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemGoalPreviewBinding;
import com.example.paisapilot.model.SavingsGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavingsPreviewAdapter extends RecyclerView.Adapter<SavingsPreviewAdapter.SavingsPreviewViewHolder> {

    private final List<SavingsGoal> goals = new ArrayList<>();

    public void setGoals(List<SavingsGoal> newGoals) {
        goals.clear();
        if (newGoals != null) {
            // Only show incomplete or recently completed
            goals.addAll(newGoals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavingsPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoalPreviewBinding binding = ItemGoalPreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SavingsPreviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsPreviewViewHolder holder, int position) {
        holder.bind(goals.get(position));
    }

    @Override
    public int getItemCount() {
        return Math.min(goals.size(), 3); // Max 3 in dashboard
    }

    class SavingsPreviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemGoalPreviewBinding binding;

        public SavingsPreviewViewHolder(@NonNull ItemGoalPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SavingsGoal goal) {
            binding.tvGoalTitle.setText(goal.getTitle());
            binding.tvGoalPercent.setText(String.format(Locale.getDefault(), "%d%%", goal.getPercentage()));
            binding.progressGoal.setProgress(goal.getPercentage());
            binding.tvGoalInfo.setText(String.format(Locale.getDefault(), "₹%.0f of ₹%.0f", goal.getSavedAmount(), goal.getTargetAmount()));
            
            binding.tvCompletedTag.setVisibility(goal.isCompleted() ? View.VISIBLE : View.GONE);
        }
    }
}
