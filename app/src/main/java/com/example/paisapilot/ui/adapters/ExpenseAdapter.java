package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemExpenseBinding;
import com.example.paisapilot.model.Expense;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenses = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnExpenseInteractionListener interactionListener;

    public interface OnExpenseInteractionListener {
        void onEditExpense(Expense expense);
        void onDeleteExpense(Expense expense);
    }

    public void setOnExpenseInteractionListener(OnExpenseInteractionListener listener) {
        this.interactionListener = listener;
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

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return format.format(amount);
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
            binding.chipExpenseCategory.setText(expense.getCategory());
            binding.tvExpenseAmount.setText("- " + formatCurrency(expense.getAmount()));
            binding.tvPaymentMethod.setText(expense.getPaymentMethod());
            
            if (expense.getDate() != null) {
                binding.tvExpenseDate.setText(dateFormat.format(expense.getDate().toDate()));
            } else {
                binding.tvExpenseDate.setText("");
            }

            binding.ivExpenseOptions.setOnClickListener(v -> showPopupMenu(v, expense));
            
            itemView.setOnClickListener(v -> {
                if (interactionListener != null) {
                    interactionListener.onEditExpense(expense);
                }
            });
        }

        private void showPopupMenu(View view, Expense expense) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");
            
            popup.setOnMenuItemClickListener(item -> {
                if (interactionListener == null) return false;
                
                if (item.getTitle().equals("Edit")) {
                    interactionListener.onEditExpense(expense);
                } else if (item.getTitle().equals("Delete")) {
                    interactionListener.onDeleteExpense(expense);
                }
                return true;
            });
            popup.show();
        }
    }
}
