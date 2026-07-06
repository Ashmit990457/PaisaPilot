package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityReportsBinding;
import com.example.paisapilot.databinding.LayoutDashboardCardBinding;
import com.example.paisapilot.model.MonthlyReport;
import com.example.paisapilot.utils.ReportExporter;
import com.example.paisapilot.viewmodel.ReportViewModel;

import java.io.IOException;
import java.util.Locale;

public class ReportsActivity extends BaseActivity {

    private ActivityReportsBinding binding;
    private ReportViewModel viewModel;
    private MonthlyReport currentReport;

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
        observeViewModel();

        binding.btnExportPDF.setOnClickListener(v -> shareReport("pdf"));
        binding.btnExportCSV.setOnClickListener(v -> shareReport("csv"));

        viewModel.loadMonthlyReport();
    }

    private void observeViewModel() {
        viewModel.getReportState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressReport.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressReport.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        currentReport = resource.getData();
                        updateUI(currentReport);
                    }
                    break;
                case ERROR:
                    binding.progressReport.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void updateUI(MonthlyReport report) {
        binding.tvReportMonth.setText(report.getMonthName());

        updateCard(binding.cardReportIncome.getRoot(), "Monthly Income", String.format(Locale.getDefault(), "₹%.2f", report.getTotalIncome()));
        updateCard(binding.cardReportExpenses.getRoot(), "Total Expenses", String.format(Locale.getDefault(), "₹%.2f", report.getTotalExpenses()));
        updateCard(binding.cardReportSavings.getRoot(), "Net Savings", String.format(Locale.getDefault(), "₹%.2f", report.getTotalSavings()));
        updateCard(binding.cardReportUtilization.getRoot(), "Budget Utilized", String.format(Locale.getDefault(), "%.1f%%", report.getBudgetUtilization()));

        binding.tvHighestCategory.setText("Highest Spending: " + report.getHighestCategory());
        if (report.getLargestExpense() != null) {
            binding.tvLargestExpense.setText(String.format(Locale.getDefault(), "Largest Expense: %s (₹%.2f)", 
                report.getLargestExpense().getTitle(), report.getLargestExpense().getAmount()));
        }
        binding.tvTransactionCount.setText("Total Transactions: " + report.getTransactionCount());
        binding.tvAiReportSummary.setText(report.getAiSummary());
    }

    private void updateCard(View view, String label, String value) {
        LayoutDashboardCardBinding cardBinding = LayoutDashboardCardBinding.bind(view);
        cardBinding.tvCardLabel.setText(label);
        cardBinding.tvCardValue.setText(value);
    }

    private void shareReport(String type) {
        if (currentReport == null) return;
        try {
            Uri uri;
            String mimeType;
            if (type.equals("pdf")) {
                uri = ReportExporter.exportToPDF(this, currentReport);
                mimeType = "application/pdf";
            } else {
                uri = ReportExporter.exportToCSV(this, currentReport);
                mimeType = "text/csv";
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Report via"));

        } catch (IOException e) {
            Toast.makeText(this, "Failed to generate report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
