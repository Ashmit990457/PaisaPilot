package com.example.paisapilot.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.databinding.ActivityMainBinding;
import com.example.paisapilot.databinding.LayoutDashboardCardBinding;
import com.example.paisapilot.databinding.LayoutForecastCardBinding;
import com.example.paisapilot.model.DashboardData;
import com.example.paisapilot.model.Expense;
import com.example.paisapilot.model.Forecast;
import com.example.paisapilot.ui.adapters.ExpenseAdapter;
import com.example.paisapilot.ui.adapters.InsightAdapter;
import com.example.paisapilot.ui.adapters.RecurringPreviewAdapter;
import com.example.paisapilot.ui.adapters.SavingsPreviewAdapter;
import com.example.paisapilot.viewmodel.DashboardViewModel;
import com.example.paisapilot.viewmodel.ExpenseViewModel;
import com.example.paisapilot.viewmodel.ForecastViewModel;
import com.example.paisapilot.viewmodel.InsightViewModel;
import com.example.paisapilot.viewmodel.RecurringViewModel;
import com.example.paisapilot.viewmodel.SavingsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private ExpenseViewModel expenseViewModel;
    private DashboardViewModel dashboardViewModel;
    private InsightViewModel insightViewModel;
    private SavingsViewModel savingsViewModel;
    private RecurringViewModel recurringViewModel;
    private ExpenseAdapter expenseAdapter;
    private InsightAdapter insightAdapter;
    private SavingsPreviewAdapter savingsPreviewAdapter;
    private RecurringPreviewAdapter recurringPreviewAdapter;
    private static final int ADD_EXPENSE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        insightViewModel = new ViewModelProvider(this).get(InsightViewModel.class);
        savingsViewModel = new ViewModelProvider(this).get(SavingsViewModel.class);
        recurringViewModel = new ViewModelProvider(this).get(RecurringViewModel.class);
        
        setupRecyclerViews();
        observeViewModels();

        binding.fabAddExpense.setOnClickListener(v -> showAddOptions());

        binding.cardBudgetSummary.setOnClickListener(v -> 
                startActivityForResult(new Intent(MainActivity.this, BudgetActivity.class), 102)
        );

        binding.cardSavingsSummary.setOnClickListener(v -> 
                startActivity(new Intent(MainActivity.this, SavingsActivity.class))
        );

        binding.cardRecurringSummary.setOnClickListener(v -> 
                startActivity(new Intent(MainActivity.this, RecurringExpensesActivity.class))
        );

        binding.btnViewReport.setOnClickListener(v -> 
                startActivity(new Intent(MainActivity.this, ReportsActivity.class))
        );

        binding.btnViewAll.setOnClickListener(v -> 
                startActivity(new Intent(MainActivity.this, AllExpensesActivity.class))
        );

        binding.ivProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadData();
    }

    private void logout() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        sessionManager.logout();
        
        new Thread(() -> {
            AppDatabase.getInstance(this).clearAllTables();
            runOnUiThread(() -> {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finishAffinity();
            });
        }).start();
    }

    private void showAddOptions() {
        String[] options = {"Add Expense Manually", "Scan Receipt"};
        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (options[which].equals("Scan Receipt")) {
                        startActivity(new Intent(this, ReceiptScannerActivity.class));
                    } else {
                        startActivityForResult(new Intent(this, AddExpenseActivity.class), ADD_EXPENSE_REQUEST);
                    }
                })
                .show();
    }

    private void loadData() {
        dashboardViewModel.loadDashboardData();
        expenseViewModel.loadExpenses();
        insightViewModel.loadInsights();
        savingsViewModel.loadGoals();
        recurringViewModel.loadRecurringExpenses();
    }

    private void setupRecyclerViews() {
        expenseAdapter = new ExpenseAdapter();
        expenseAdapter.setOnExpenseInteractionListener(new ExpenseAdapter.OnExpenseInteractionListener() {
            @Override
            public void onEditExpense(Expense expense) {
                editExpense(expense);
            }

            @Override
            public void onDeleteExpense(Expense expense) {
                deleteExpenseWithUndo(expense);
            }
        });
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(expenseAdapter);

        insightAdapter = new InsightAdapter();
        binding.rvInsights.setLayoutManager(new LinearLayoutManager(this));
        binding.rvInsights.setAdapter(insightAdapter);

        savingsPreviewAdapter = new SavingsPreviewAdapter();
        binding.rvSavingsPreview.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSavingsPreview.setAdapter(savingsPreviewAdapter);

        recurringPreviewAdapter = new RecurringPreviewAdapter();
        binding.rvRecurringPreview.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecurringPreview.setAdapter(recurringPreviewAdapter);
    }

    private void editExpense(Expense expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("edit_expense_id", expense.getExpenseId());
        intent.putExtra("edit_title", expense.getTitle());
        intent.putExtra("edit_amount", String.valueOf(expense.getAmount()));
        intent.putExtra("edit_category", expense.getCategory());
        intent.putExtra("edit_payment", expense.getPaymentMethod());
        intent.putExtra("edit_note", expense.getNote());
        intent.putExtra("edit_user_id", expense.getUserId());
        if (expense.getDate() != null) {
            intent.putExtra("edit_date", expense.getDate().toDate().getTime());
        }
        if (expense.getCreatedAt() != null) {
            intent.putExtra("edit_created_at", expense.getCreatedAt().toDate().getTime());
        }
        startActivityForResult(intent, ADD_EXPENSE_REQUEST);
    }

    private void deleteExpenseWithUndo(Expense expense) {
        expenseViewModel.deleteExpense(expense.getExpenseId());
        
        Snackbar.make(binding.main, "Expense deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    expenseViewModel.undoDelete(expense.getExpenseId());
                })
                .show();
    }

    private void observeViewModels() {
        dashboardViewModel.getUserNameState().observe(this, name -> {
            if (name != null) {
                binding.tvUsername.setText(name);
            }
        });

        dashboardViewModel.getDashboardState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressMain.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressMain.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        updateDashboardUI(resource.getData());
                    }
                    break;
                case ERROR:
                    binding.progressMain.setVisibility(View.GONE);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        expenseViewModel.getExpensesState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        int limit = Math.min(resource.getData().size(), 5);
                        expenseAdapter.setExpenses(resource.getData().subList(0, limit));
                        binding.rvExpenses.setVisibility(View.VISIBLE);
                        binding.emptyState.setVisibility(View.GONE);
                    } else {
                        binding.rvExpenses.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        });

        insightViewModel.getInsightsState().observe(this, resource -> {
            if (resource == null) return;
            if (resource.getStatus() == com.example.paisapilot.model.Resource.Status.SUCCESS) {
                if (resource.getData() != null) {
                    insightAdapter.setInsights(resource.getData());
                }
            }
        });

        savingsViewModel.getGoalsState().observe(this, resource -> {
            if (resource == null) return;
            if (resource.getStatus() == com.example.paisapilot.model.Resource.Status.SUCCESS) {
                if (resource.getData() != null) {
                    savingsPreviewAdapter.setGoals(resource.getData());
                }
            }
        });

        recurringViewModel.getRecurringListState().observe(this, resource -> {
            if (resource == null) return;
            if (resource.getStatus() == com.example.paisapilot.model.Resource.Status.SUCCESS) {
                if (resource.getData() != null) {
                    recurringPreviewAdapter.setList(resource.getData());
                }
            }
        });
    }

    private void updateDashboardUI(DashboardData data) {
        updateCard(binding.cardTotalExpense, "Total Expenses", String.format(Locale.getDefault(), "₹%.2f", data.getTotalExpense()));
        updateCard(binding.cardTotalBudget, "Total Budget", String.format(Locale.getDefault(), "₹%.2f", data.getTotalBudget()));
        updateCard(binding.cardRemainingBudget, "Remaining Budget", String.format(Locale.getDefault(), "₹%.2f", data.getRemainingBudget()));
        updateCard(binding.cardExpenseCount, "Expenses Count", String.valueOf(data.getExpenseCount()));
        updateCard(binding.cardHighestCategory, "Highest Category", data.getHighestCategory());

        if (data.getForecast() != null) {
            updateForecastUI(data.getForecast());
        }

        setupPieChart(data.getCategoryWiseExpenses());
        setupBarChart(data.getWeeklyExpenses());
    }

    private void updateCard(LayoutDashboardCardBinding cardBinding, String label, String value) {
        cardBinding.tvCardLabel.setText(label);
        cardBinding.tvCardValue.setText(value);
    }

    private void updateForecastUI(Forecast forecast) {
        LayoutForecastCardBinding forecastBinding = LayoutForecastCardBinding.bind(binding.layoutForecast.getRoot());
        
        forecastBinding.tvAverageDailySpend.setText(String.format(Locale.getDefault(), "₹%.2f", forecast.getAverageDailySpending()));
        forecastBinding.tvProjectedExpense.setText(String.format(Locale.getDefault(), "₹%.0f", forecast.getProjectedExpense()));
        forecastBinding.tvProjectedSavings.setText(String.format(Locale.getDefault(), "₹%.0f", forecast.getForecastedSavings()));
        forecastBinding.tvForecastRecommendation.setText(forecast.getForecastMessage());

        String status = forecast.getRiskLevel().name();
        forecastBinding.chipSpendingStatus.setText(status);

        int color;
        int bgColor;
        switch (forecast.getRiskLevel()) {
            case SAFE:
                color = Color.parseColor("#15803D");
                bgColor = Color.parseColor("#DCFCE7");
                break;
            case MODERATE:
                color = Color.parseColor("#92400E");
                bgColor = Color.parseColor("#FEF3C7");
                break;
            case HIGH:
            default:
                color = Color.parseColor("#991B1B");
                bgColor = Color.parseColor("#FEE2E2");
                break;
        }
        forecastBinding.chipSpendingStatus.setTextColor(color);
        forecastBinding.chipSpendingStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(bgColor));
    }

    private void setupPieChart(Map<String, Double> categoryData) {
        PieChart pieChart = binding.pieChart;
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupBarChart(Map<Integer, Double> weeklyData) {
        BarChart barChart = binding.barChart;
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            float val = weeklyData.getOrDefault(i, 0.0).floatValue();
            entries.add(new BarEntry(i, val));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Weekly Spending");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_EXPENSE_REQUEST || requestCode == 102) && resultCode == RESULT_OK) {
            loadData();
        }
    }
}
