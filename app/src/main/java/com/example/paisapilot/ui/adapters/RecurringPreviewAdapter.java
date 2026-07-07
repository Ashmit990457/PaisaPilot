package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemRecurringPreviewBinding;
import com.example.paisapilot.model.RecurringExpense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecurringPreviewAdapter extends RecyclerView.Adapter<RecurringPreviewAdapter.RecurringPreviewViewHolder> {

    private final List<RecurringExpense> list = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public void setList(List<RecurringExpense> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecurringPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecurringPreviewBinding binding = ItemRecurringPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecurringPreviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringPreviewViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), 3);
    }

    class RecurringPreviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecurringPreviewBinding binding;

        public RecurringPreviewViewHolder(@NonNull ItemRecurringPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecurringExpense item) {
            binding.tvBillTitle.setText(item.getTitle());
            binding.tvBillAmount.setText(String.format(Locale.getDefault(), "₹%.2f", item.getAmount()));
            
            if (item.getNextDueDate() != null) {
                long diff = item.getNextDueDate().toDate().getTime() - System.currentTimeMillis();
                long days = diff / (24 * 60 * 60 * 1000);
                
                String dueText;
                if (days == 0) dueText = "Due Today";
                else if (days == 1) dueText = "Due Tomorrow";
                else if (days < 0) dueText = "Overdue by " + Math.abs(days) + " days";
                else dueText = "Due in " + days + " days (" + dateFormat.format(item.getNextDueDate().toDate()) + ")";
                
                binding.tvBillDue.setText(dueText);
                binding.vOverdueIndicator.setVisibility(days < 0 ? View.VISIBLE : View.GONE);
                if (days < 0) {
                    binding.tvBillDue.setTextColor(android.graphics.Color.parseColor("#E11D48"));
                } else {
                    binding.tvBillDue.setTextColor(android.graphics.Color.parseColor("#64748B"));
                }
            }
        }
    }
}
