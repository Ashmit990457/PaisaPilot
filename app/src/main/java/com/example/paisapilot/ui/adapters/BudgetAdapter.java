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

import java.text.NumberFormat;
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

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return format.format(amount);
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetBinding binding;

        public BudgetViewHolder(@NonNull ItemBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Budget budget) {
            binding.tvBudgetCategory.setText(budget.getCategory());
            binding.tvBudgetLimit.setText(String.format(Locale.getDefault(), "Limit: %s", formatCurrency(budget.getMonthlyLimit())));
            binding.tvBudgetSpent.setText(formatCurrency(budget.getSpentAmount()));
            binding.tvBudgetRemaining.setText(formatCurrency(budget.getRemainingAmount()));

            int progress = (int) ((budget.getSpentAmount() / budget.getMonthlyLimit()) * 100);
            binding.progressBudgetUsage.setProgress(Math.min(progress, 100), true);

            // Coloring logic
            int color;
            if (progress < 70) {
                color = Color.parseColor("#22C55E"); // accent_green
            } else if (progress <= 90) {
                color = Color.parseColor("#F59E0B"); // amber
            } else {
                color = Color.parseColor("#EF4444"); // red
            }
            binding.progressBudgetUsage.setIndicatorColor(color);
            binding.tvBudgetRemaining.setTextColor(color);

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
