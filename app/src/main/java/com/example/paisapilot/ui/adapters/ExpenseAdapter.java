package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemExpenseBinding;
import com.example.paisapilot.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenses = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnExpenseLongClickListener longClickListener;

    public interface OnExpenseLongClickListener {
        void onExpenseLongClick(Expense expense);
    }

    public void setOnExpenseLongClickListener(OnExpenseLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setExpenses(List<Expense> newExpenses) {
        expenses.clear();
        if (newExpenses != null) {
            expenses.addAll(newExpenses);
        }
        notifyDataSetChanged();
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        public ExpenseViewHolder(@NonNull ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Expense expense) {
            binding.tvExpenseTitle.setText(expense.getTitle());
            binding.tvExpenseCategory.setText(expense.getCategory());
            binding.tvExpenseAmount.setText(String.format(Locale.getDefault(), "-₹%.2f", expense.getAmount()));
            
            if (expense.getDate() != null) {
                binding.tvExpenseDate.setText(dateFormat.format(expense.getDate().toDate()));
            } else {
                binding.tvExpenseDate.setText("");
            }

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onExpenseLongClick(expense);
                    return true;
                }
                return false;
            });
        }
    }
}
