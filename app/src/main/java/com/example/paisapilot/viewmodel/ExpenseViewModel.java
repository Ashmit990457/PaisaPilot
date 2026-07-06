package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
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
    private final MutableLiveData<Resource<Boolean>> addExpenseState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> deleteExpenseState = new MutableLiveData<>();

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ExpenseRepository(application);
        
        // Connect LiveData from Repository to ViewModel state
        expensesState.addSource(repository.getAllExpenses(), expenses -> {
            expensesState.setValue(Resource.success(expenses));
        });
    }

    public LiveData<Resource<List<Expense>>> getExpensesState() {
        return expensesState;
    }

    public LiveData<Resource<Boolean>> getAddExpenseState() {
        return addExpenseState;
    }

    public LiveData<Resource<Boolean>> getDeleteExpenseState() {
        return deleteExpenseState;
    }

    public ValidationResult validateExpenseInput(@NonNull String title, @NonNull String category, double amount) {
        if (title.trim().isEmpty()) return ValidationResult.error("Title is required");
        if (amount <= 0) return ValidationResult.error("Amount must be greater than 0");
        if (category.trim().isEmpty()) return ValidationResult.error("Category is required");
        return ValidationResult.success();
    }

    public void addExpense(@NonNull String title, @NonNull String category, @NonNull String amountText, @NonNull Timestamp date, @NonNull String note, @NonNull String paymentMethod) {
        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            addExpenseState.setValue(Resource.error("Enter a valid amount"));
            return;
        }

        ValidationResult validation = validateExpenseInput(title, category, amount);
        if (!validation.isSuccess()) {
            addExpenseState.setValue(Resource.error(validation.getErrorMessage()));
            return;
        }

        Expense expense = new Expense();
        expense.setTitle(title.trim());
        expense.setCategory(category.trim());
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setNote(note.trim());
        expense.setPaymentMethod(paymentMethod.trim());

        addExpenseState.setValue(Resource.loading());
        repository.addExpense(expense, new ExpenseRepository.ExpenseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                addExpenseState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                addExpenseState.postValue(Resource.error(message));
            }
        });
    }

    public void deleteExpense(@NonNull String expenseId) {
        deleteExpenseState.setValue(Resource.loading());
        repository.deleteExpense(expenseId, new ExpenseRepository.ExpenseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                deleteExpenseState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                deleteExpenseState.postValue(Resource.error(message));
            }
        });
    }

    public void loadExpenses() {
        // Now automatically observed from Repository via Room
    }
}
