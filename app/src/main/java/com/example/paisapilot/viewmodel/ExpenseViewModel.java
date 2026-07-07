package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.Expense;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.ValidationResult;
import com.example.paisapilot.repository.ExpenseRepository;
import com.google.firebase.Timestamp;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final MediatorLiveData<Resource<List<Expense>>> expensesState = new MediatorLiveData<>();
    private final MutableLiveData<Resource<Boolean>> expenseActionState = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> dateFilter = new MutableLiveData<>(null);
    private final MutableLiveData<List<String>> paymentFilters = new MutableLiveData<>(new java.util.ArrayList<>());
    private final MutableLiveData<String> sortBy = new MutableLiveData<>("Newest First");

    private LiveData<List<Expense>> currentSource;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ExpenseRepository(application);
        refreshSource();
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        refreshSource();
    }

    public void setFilters(String date, List<String> payments, String sort) {
        dateFilter.setValue(date);
        paymentFilters.setValue(payments);
        sortBy.setValue(sort);
        refreshSource();
    }

    private void refreshSource() {
        if (currentSource != null) {
            expensesState.removeSource(currentSource);
        }
        currentSource = repository.getFilteredExpenses(searchQuery.getValue(), dateFilter.getValue(), paymentFilters.getValue(), sortBy.getValue());
        expensesState.addSource(currentSource, expenses -> {
            expensesState.setValue(Resource.success(expenses));
        });
    }

    public void clearFilters() {
        searchQuery.setValue("");
        dateFilter.setValue(null);
        paymentFilters.setValue(new java.util.ArrayList<>());
        sortBy.setValue("Newest First");
        refreshSource();
    }

    public LiveData<Resource<List<Expense>>> getExpensesState() {
        return expensesState;
    }

    public LiveData<Resource<Boolean>> getAddExpenseState() {
        return expenseActionState;
    }

    public LiveData<Resource<Boolean>> getDeleteExpenseState() {
        return expenseActionState;
    }

    public ValidationResult validateExpenseInput(@NonNull String title, @NonNull String category, double amount) {
        if (title.trim().isEmpty()) return ValidationResult.error("Title is required");
        if (amount <= 0) return ValidationResult.error("Amount must be greater than 0");
        if (category.trim().isEmpty()) return ValidationResult.error("Category is required");
        return ValidationResult.success();
    }

    public void addExpense(@NonNull String title, @NonNull String category, @NonNull String amountText, @NonNull Timestamp date, @NonNull String note, @NonNull String paymentMethod) {
        saveExpense(null, title, category, amountText, date, note, paymentMethod, null, null);
    }

    public void saveExpense(@Nullable String id, @NonNull String title, @NonNull String category, @NonNull String amountText, @NonNull Timestamp date, @NonNull String note, @NonNull String paymentMethod, @Nullable String userId, @Nullable Timestamp createdAt) {
        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            expenseActionState.setValue(Resource.error("Enter a valid amount"));
            return;
        }

        ValidationResult validation = validateExpenseInput(title, category, amount);
        if (!validation.isSuccess()) {
            expenseActionState.setValue(Resource.error(validation.getErrorMessage()));
            return;
        }

        Expense expense = new Expense();
        expense.setExpenseId(id);
        expense.setTitle(title.trim());
        expense.setCategory(category.trim());
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setNote(note.trim());
        expense.setPaymentMethod(paymentMethod.trim());
        expense.setUserId(userId);
        expense.setCreatedAt(createdAt);

        expenseActionState.setValue(Resource.loading());
        ExpenseRepository.ExpenseCallback<Boolean> callback = new ExpenseRepository.ExpenseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                expenseActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                expenseActionState.postValue(Resource.error(message));
            }
        };

        if (id == null) {
            repository.addExpense(expense, callback);
        } else {
            repository.updateExpense(expense, callback);
        }
    }

    public void deleteExpense(@NonNull String expenseId) {
        repository.deleteExpense(expenseId, new ExpenseRepository.ExpenseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                // No need for expenseActionState success if we want instant deletion
            }

            @Override
            public void onError(@NonNull String message) {
                expenseActionState.postValue(Resource.error(message));
            }
        });
    }

    public void undoDelete(@NonNull String expenseId) {
        repository.undoDelete(expenseId);
    }

    public void loadExpenses() {
    }
}
