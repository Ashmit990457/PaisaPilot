package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
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
            // Show top 3
            int limit = Math.min(newGoals.size(), 3);
            goals.addAll(newGoals.subList(0, limit));
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
        return goals.size();
    }

    static class SavingsPreviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemGoalPreviewBinding binding;

        public SavingsPreviewViewHolder(@NonNull ItemGoalPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SavingsGoal goal) {
            binding.tvGoalTitlePreview.setText(goal.getTitle());
            binding.tvGoalPercentagePreview.setText(String.format(Locale.getDefault(), "%d%%", goal.getPercentage()));
            binding.progressGoalPreview.setProgress(goal.getPercentage());
            binding.tvRemainingPreview.setText(String.format(Locale.getDefault(), "₹%.2f remaining", goal.getRemainingAmount()));
        }
    }
}
