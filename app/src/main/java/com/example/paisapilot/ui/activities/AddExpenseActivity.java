package com.example.paisapilot.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityAddExpenseBinding;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.utils.AppConstants;
import com.example.paisapilot.viewmodel.ExpenseViewModel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private ActivityAddExpenseBinding binding;
    private ExpenseViewModel viewModel;
    private Timestamp selectedDate;
    private boolean isSaving = false;
    private String expenseIdToEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        selectedDate = Timestamp.now();

        setupCategorySpinner();
        setupPaymentMethodSpinner();
        setupDatePicker();
        observeViewModel();
        prefillFromIntent();

        binding.btnSaveExpense.setOnClickListener(v -> onSaveExpenseClicked());
    }

    private void prefillFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        expenseIdToEdit = intent.getStringExtra("edit_expense_id");
        if (expenseIdToEdit != null) {
            getSupportActionBar().setTitle("Edit Expense");
            binding.btnSaveExpense.setText("Update Transaction");
            
            binding.etExpenseTitle.setText(intent.getStringExtra("edit_title"));
            binding.etExpenseAmount.setText(intent.getStringExtra("edit_amount"));
            binding.spinnerCategory.setText(intent.getStringExtra("edit_category"), false);
            binding.spinnerPaymentMethod.setText(intent.getStringExtra("edit_payment"), false);
            binding.etExpenseNote.setText(intent.getStringExtra("edit_note"));
            
            long dateLong = intent.getLongExtra("edit_date", -1);
            if (dateLong != -1) {
                Date date = new Date(dateLong);
                selectedDate = new Timestamp(date);
                updateSelectedDateText(date);
            }
        }

        String pTitle = intent.getStringExtra("prefill_title");
        if (pTitle != null) binding.etExpenseTitle.setText(pTitle);
        String pAmount = intent.getStringExtra("prefill_amount");
        if (pAmount != null) binding.etExpenseAmount.setText(pAmount);
        String pCat = intent.getStringExtra("prefill_category");
        if (pCat != null) binding.spinnerCategory.setText(pCat, false);
        String pPay = intent.getStringExtra("prefill_payment");
        if (pPay != null) binding.spinnerPaymentMethod.setText(pPay, false);

        String dateStr = intent.getStringExtra("prefill_date");
        if (dateStr != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(dateStr);
                if (date != null) {
                    selectedDate = new Timestamp(date);
                    updateSelectedDateText(date);
                }
            } catch (Exception ignored) {}
        }
        
        int confidence = intent.getIntExtra("confidence", -1);
        if (confidence != -1) {
            String msg;
            if (confidence >= 90) msg = "✓ High Confidence Scan";
            else if (confidence >= 70) msg = "⚠ Please verify detected information";
            else msg = "AI could not confidently read this receipt.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void setupCategorySpinner() {
        String[] items = AppConstants.EXPENSE_CATEGORIES.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupPaymentMethodSpinner() {
        String[] items = AppConstants.PAYMENT_METHODS.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        binding.spinnerPaymentMethod.setAdapter(adapter);
    }

    private void setupDatePicker() {
        updateSelectedDateText(selectedDate.toDate());
        binding.etExpenseDate.setOnClickListener(v -> openDatePicker());
        binding.etExpenseDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) openDatePicker();
        });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate.toDate());

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    Date date = selected.getTime();
                    selectedDate = new Timestamp(date);
                    updateSelectedDateText(date);
                    binding.etExpenseDate.clearFocus();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateSelectedDateText(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.etExpenseDate.setText(sdf.format(date));
    }

    private void observeViewModel() {
        viewModel.getAddExpenseState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressSaveExpense.setVisibility(View.VISIBLE);
                    binding.btnSaveExpense.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressSaveExpense.setVisibility(View.GONE);
                    Toast.makeText(this, expenseIdToEdit == null ? "Expense saved" : "Expense updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    isSaving = false;
                    binding.progressSaveExpense.setVisibility(View.GONE);
                    binding.btnSaveExpense.setEnabled(true);
                    Toast.makeText(this, "Error: " + resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void onSaveExpenseClicked() {
        if (isSaving) return;

        String title = binding.etExpenseTitle.getText() != null ? binding.etExpenseTitle.getText().toString() : "";
        String amount = binding.etExpenseAmount.getText() != null ? binding.etExpenseAmount.getText().toString() : "";
        String note = binding.etExpenseNote.getText() != null ? binding.etExpenseNote.getText().toString() : "";
        String category = binding.spinnerCategory.getText().toString();
        String paymentMethod = binding.spinnerPaymentMethod.getText().toString();

        isSaving = true;
        binding.btnSaveExpense.setEnabled(false);
        binding.progressSaveExpense.setVisibility(View.VISIBLE);

        viewModel.saveExpense(expenseIdToEdit, title, category, amount, selectedDate, note, paymentMethod);
    }
}
