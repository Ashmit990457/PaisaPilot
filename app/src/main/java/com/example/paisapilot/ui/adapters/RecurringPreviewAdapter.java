package com.example.paisapilot.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemRecurringPreviewBinding;
import com.example.paisapilot.model.RecurringExpense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecurringPreviewAdapter extends RecyclerView.Adapter<RecurringPreviewAdapter.RecurringPreviewViewHolder> {

    private final List<RecurringExpense> list = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public void setList(List<RecurringExpense> newList) {
        list.clear();
        if (newList != null) {
            int limit = Math.min(newList.size(), 3);
            list.addAll(newList.subList(0, limit));
        }
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
        return list.size();
    }

    class RecurringPreviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecurringPreviewBinding binding;

        public RecurringPreviewViewHolder(@NonNull ItemRecurringPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecurringExpense item) {
            binding.tvRecurringTitlePreview.setText(item.getTitle());
            binding.tvRecurringAmountPreview.setText(String.format(Locale.getDefault(), "₹%.2f", item.getAmount()));
            if (item.getNextDueDate() != null) {
                binding.tvRecurringDuePreview.setText(String.format(Locale.getDefault(), "Due on %s", dateFormat.format(item.getNextDueDate().toDate())));
            }
        }
    }
}
