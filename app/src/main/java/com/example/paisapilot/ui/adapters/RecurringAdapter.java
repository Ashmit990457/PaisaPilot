package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemRecurringBinding;
import com.example.paisapilot.model.RecurringExpense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.RecurringViewHolder> {

    private final List<RecurringExpense> list = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnRecurringInteractionListener listener;

    public interface OnRecurringInteractionListener {
        void onDelete(RecurringExpense recurring);
    }

    public void setOnRecurringInteractionListener(OnRecurringInteractionListener listener) {
        this.listener = listener;
    }

    public void setList(List<RecurringExpense> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecurringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecurringBinding binding = ItemRecurringBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecurringViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class RecurringViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecurringBinding binding;

        public RecurringViewHolder(@NonNull ItemRecurringBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecurringExpense item) {
            binding.tvRecurringTitle.setText(item.getTitle());
            binding.tvRecurringAmount.setText(String.format(Locale.getDefault(), "₹%.2f", item.getAmount()));
            binding.tvRecurringFrequency.setText(item.getFrequency().name());
            
            if (item.getNextDueDate() != null) {
                binding.tvNextDueInfo.setText(String.format(Locale.getDefault(), "Next due: %s", dateFormat.format(item.getNextDueDate().toDate())));
            }

            binding.chipReminder.setVisibility(item.isReminderEnabled() ? View.VISIBLE : View.GONE);
            binding.chipAutoAdd.setVisibility(item.isAutoAddExpense() ? View.VISIBLE : View.GONE);

            binding.btnDeleteRecurring.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(item);
            });
        }
    }
}
