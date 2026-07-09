package com.example.paisapilot.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityReportsBinding;
import com.example.paisapilot.databinding.LayoutDashboardCardBinding;
import com.example.paisapilot.model.MonthlyReport;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.utils.ExportUtils;
import com.example.paisapilot.viewmodel.ReportViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportsActivity extends BaseActivity {

    private ActivityReportsBinding binding;
    private ReportViewModel viewModel;
    private Calendar selectedMonthCalendar;
    private MonthlyReport currentReport;
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat monthIdFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.reportsRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbarReports);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarReports.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        selectedMonthCalendar = Calendar.getInstance();

        setupMonthNavigator();
        observeViewModel();
        loadReport();
        
        binding.btnExportPDF.setOnClickListener(v -> {
            if (currentReport != null) {
                ExportUtils.exportReportToPDF(this, currentReport);
            } else {
                Toast.makeText(this, "Report not loaded", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnExportCSV.setOnClickListener(v -> {
            if (currentReport != null) {
                ExportUtils.exportReportToCSV(this, currentReport);
            } else {
                Toast.makeText(this, "Report not loaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMonthNavigator() {
        binding.layoutMonthNavigator.btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        binding.layoutMonthNavigator.btnNextMonth.setOnClickListener(v -> navigateMonth(1));
        updateNavigatorUI();
    }

    private void navigateMonth(int delta) {
        selectedMonthCalendar.add(Calendar.MONTH, delta);
        updateNavigatorUI();
        loadReport();
        
        // Animate content
        binding.scrollViewReport.setAlpha(0f);
        binding.scrollViewReport.animate().alpha(1f).setDuration(300).start();
        binding.layoutEmptyState.setAlpha(0f);
        binding.layoutEmptyState.animate().alpha(1f).setDuration(300).start();
    }

    private void updateNavigatorUI() {
        Calendar current = Calendar.getInstance();
        boolean isCurrentMonth = selectedMonthCalendar.get(Calendar.MONTH) == current.get(Calendar.MONTH) &&
                selectedMonthCalendar.get(Calendar.YEAR) == current.get(Calendar.YEAR);

        binding.layoutMonthNavigator.tvSelectedMonth.setText(monthYearFormat.format(selectedMonthCalendar.getTime()));
        binding.layoutMonthNavigator.tvMonthStatus.setText(isCurrentMonth ? "Viewing Current Month" : "Viewing Archived Month");
        
        // Disable next button if viewing current or future month
        binding.layoutMonthNavigator.btnNextMonth.setEnabled(!isCurrentMonth);
        binding.layoutMonthNavigator.btnNextMonth.setAlpha(isCurrentMonth ? 0.5f : 1.0f);
    }

    private void loadReport() {
        String monthId = monthIdFormat.format(selectedMonthCalendar.getTime());
        viewModel.loadReport(monthId);
    }

    private void observeViewModel() {
        viewModel.getReportState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressReport.setVisibility(View.VISIBLE);
                    binding.scrollViewReport.setVisibility(View.GONE);
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressReport.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        this.currentReport = resource.getData();
                        binding.scrollViewReport.setVisibility(View.VISIBLE);
                        binding.layoutEmptyState.setVisibility(View.GONE);
                        updateUI(resource.getData());
                    } else {
                        this.currentReport = null;
                        showEmptyState();
                    }
                    break;
                case ERROR:
                    binding.progressReport.setVisibility(View.GONE);
                    showEmptyState();
                    if (resource.getMessage() != null && !resource.getMessage().contains("No data")) {
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
    }

    private void showEmptyState() {
        binding.scrollViewReport.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.tvEmptyTitle.setText("No data available for " + monthYearFormat.format(selectedMonthCalendar.getTime()));
    }

    private void updateUI(MonthlyReport report) {
        binding.tvReportMonth.setText(report.getMonthName());
        updateCard(binding.cardReportIncome.getRoot(), "Monthly Income", String.format(Locale.getDefault(), "₹%.2f", report.getTotalIncome()));
        updateCard(binding.cardReportExpenses.getRoot(), "Total Expenses", String.format(Locale.getDefault(), "₹%.2f", report.getTotalExpenses()));
        updateCard(binding.cardReportSavings.getRoot(), "Monthly Savings", String.format(Locale.getDefault(), "₹%.2f", report.getTotalSavings()));
        updateCard(binding.cardReportUtilization.getRoot(), "Budget Usage", String.format(Locale.getDefault(), "%.1f%%", report.getBudgetUtilization()));

        binding.tvHighestCategory.setText("Highest Spending: " + report.getHighestCategory());
        if (report.getLargestExpense() != null) {
            binding.tvLargestExpense.setText(String.format(Locale.getDefault(), "Largest Expense: %s (₹%.2f)", 
                    report.getLargestExpense().getTitle(), report.getLargestExpense().getAmount()));
        } else {
            binding.tvLargestExpense.setText("Largest Expense: None");
        }
        binding.tvTransactionCount.setText("Total Transactions: " + report.getTransactionCount());
        binding.tvAiReportSummary.setText(report.getAiSummary());
    }

    private void updateCard(View cardView, String label, String value) {
        LayoutDashboardCardBinding cardBinding = LayoutDashboardCardBinding.bind(cardView);
        cardBinding.tvCardLabel.setText(label);
        cardBinding.tvCardValue.setText(value);
    }
}
