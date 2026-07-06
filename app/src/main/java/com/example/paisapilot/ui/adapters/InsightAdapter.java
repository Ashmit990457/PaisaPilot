package com.example.paisapilot.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisapilot.databinding.ItemInsightBinding;
import com.example.paisapilot.model.Insight;

import java.util.ArrayList;
import java.util.List;

public class InsightAdapter extends RecyclerView.Adapter<InsightAdapter.InsightViewHolder> {

    private final List<Insight> insights = new ArrayList<>();

    public void setInsights(List<Insight> newInsights) {
        insights.clear();
        if (newInsights != null) {
            insights.addAll(newInsights);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InsightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInsightBinding binding = ItemInsightBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new InsightViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InsightViewHolder holder, int position) {
        holder.bind(insights.get(position));
    }

    @Override
    public int getItemCount() {
        return insights.size();
    }

    static class InsightViewHolder extends RecyclerView.ViewHolder {
        private final ItemInsightBinding binding;

        public InsightViewHolder(@NonNull ItemInsightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Insight insight) {
            binding.tvInsightTitle.setText(insight.getTitle());
            binding.tvInsightDescription.setText(insight.getDescription());

            int color;
            int iconRes;

            switch (insight.getType()) {
                case WARNING:
                    color = Color.RED;
                    iconRes = android.R.drawable.ic_dialog_alert;
                    break;
                case SUCCESS:
                    color = Color.GREEN;
                    iconRes = android.R.drawable.checkbox_on_background;
                    break;
                case INFO:
                    color = Color.BLUE;
                    iconRes = android.R.drawable.ic_dialog_info;
                    break;
                case TIP:
                    color = Color.rgb(255, 165, 0); // Orange
                    iconRes = android.R.drawable.ic_menu_help;
                    break;
                default:
                    color = Color.GRAY;
                    iconRes = android.R.drawable.ic_dialog_info;
                    break;
            }

            binding.viewPriorityBorder.setBackgroundColor(color);
            binding.ivInsightIcon.setImageResource(iconRes);
            binding.ivInsightIcon.setColorFilter(color);
        }
    }
}
