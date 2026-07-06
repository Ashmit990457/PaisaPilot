package com.example.paisapilot.ui.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityAddRecurringBinding;
import com.example.paisapilot.model.RecurringExpense;
import com.example.paisapilot.utils.AppConstants;
import com.example.paisapilot.viewmodel.RecurringViewModel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddRecurringExpenseActivity extends BaseActivity {

    private ActivityAddRecurringBinding binding;
    private RecurringViewModel viewModel;
    private Timestamp selectedDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddRecurringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.addRecurringRoot, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RecurringViewModel.class);

        setupFrequencySpinner();
        setupDatePicker();
        observeViewModel();

        binding.btnSaveRecurring.setOnClickListener(v -> onSaveClicked());
    }

    private void setupFrequencySpinner() {
        String[] items = AppConstants.RECURRING_FREQUENCIES.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        binding.spinnerFrequency.setAdapter(adapter);
    }

    private void setupDatePicker() {
        binding.etNextDueDate.setOnClickListener(v -> openDatePicker());
        binding.etNextDueDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) openDatePicker();
        });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            Date date = selected.getTime();
            selectedDate = new Timestamp(date);
            binding.etNextDueDate.setText(dateFormat.format(date));
            binding.etNextDueDate.clearFocus();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void observeViewModel() {
        viewModel.getRecurringActionState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressSaveRecurring.setVisibility(View.VISIBLE);
                    binding.btnSaveRecurring.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressSaveRecurring.setVisibility(View.GONE);
                    binding.btnSaveRecurring.setEnabled(true);
                    Toast.makeText(this, "Recurring bill saved!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    binding.progressSaveRecurring.setVisibility(View.GONE);
                    binding.btnSaveRecurring.setEnabled(true);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void onSaveClicked() {
        String title = binding.etRecurringTitle.getText() != null ? binding.etRecurringTitle.getText().toString() : "";
        String amount = binding.etRecurringAmount.getText() != null ? binding.etRecurringAmount.getText().toString() : "";
        
        String freqStr = binding.spinnerFrequency.getText() != null ? binding.spinnerFrequency.getText().toString() : "";
        RecurringExpense.Frequency frequency = null;
        try {
            frequency = RecurringExpense.Frequency.valueOf(freqStr.toUpperCase());
        } catch (Exception ignored) {}

        boolean reminder = binding.switchReminder.isChecked();
        boolean autoAdd = binding.switchAutoAdd.isChecked();

        viewModel.createRecurring(title, "Other", amount, frequency, selectedDate, reminder, autoAdd);
    }
}
