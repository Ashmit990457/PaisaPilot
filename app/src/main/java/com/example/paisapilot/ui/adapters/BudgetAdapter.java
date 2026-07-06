package com.example.paisapilot.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemBudgetBinding;
import com.example.paisapilot.model.Budget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<Budget> budgets = new ArrayList<>();
    private OnBudgetLongClickListener longClickListener;

    public interface OnBudgetLongClickListener {
        void onBudgetLongClick(Budget budget);
    }

    public void setOnBudgetLongClickListener(OnBudgetLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setBudgets(List<Budget> newBudgets) {
        budgets.clear();
        if (newBudgets != null) {
            budgets.addAll(newBudgets);
        }
        notifyDataSetChanged();
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBudgetBinding binding = ItemBudgetBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BudgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        holder.bind(budgets.get(position));
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetBinding binding;

        public BudgetViewHolder(@NonNull ItemBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Budget budget) {
            binding.tvBudgetCategory.setText(budget.getCategory());
            binding.tvBudgetLimit.setText(String.format(Locale.getDefault(), "₹%.0f Budget", budget.getMonthlyLimit()));
            binding.tvBudgetSpent.setText(String.format(Locale.getDefault(), "₹%.0f", budget.getSpentAmount()));
            binding.tvBudgetRemaining.setText(String.format(Locale.getDefault(), "₹%.0f", budget.getRemainingAmount()));

            int progress = (int) ((budget.getSpentAmount() / budget.getMonthlyLimit()) * 100);
            binding.progressBudgetUsage.setProgress(Math.min(progress, 100));

            // Coloring logic
            int color;
            if (progress < 70) {
                color = Color.GREEN;
            } else if (progress <= 90) {
                color = Color.YELLOW;
            } else {
                color = Color.RED;
            }
            binding.progressBudgetUsage.setIndicatorColor(color);

            if (budget.getSpentAmount() > budget.getMonthlyLimit()) {
                binding.tvBudgetWarning.setVisibility(View.VISIBLE);
            } else {
                binding.tvBudgetWarning.setVisibility(View.GONE);
            }

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onBudgetLongClick(budget);
                    return true;
                }
                return false;
            });
        }
    }
}
