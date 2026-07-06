package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.Budget;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.ValidationResult;
import com.example.paisapilot.repository.BudgetRepository;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository repository;
    private final MediatorLiveData<Resource<List<Budget>>> budgetsState = new MediatorLiveData<>();
    private final MutableLiveData<Resource<Boolean>> createBudgetState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> deleteBudgetState = new MutableLiveData<>();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        this.repository = new BudgetRepository(application);
        
        budgetsState.addSource(repository.getAllBudgets(), budgets -> {
            budgetsState.setValue(Resource.success(budgets));
        });
    }

    public LiveData<Resource<List<Budget>>> getBudgetsState() {
        return budgetsState;
    }

    public LiveData<Resource<Boolean>> getCreateBudgetState() {
        return createBudgetState;
    }

    public LiveData<Resource<Boolean>> getDeleteBudgetState() {
        return deleteBudgetState;
    }

    public ValidationResult validateBudgetInput(String limitText) {
        if (limitText == null || limitText.trim().isEmpty()) return ValidationResult.error("Monthly limit is required");
        try {
            double limit = Double.parseDouble(limitText.trim());
            if (limit <= 0) return ValidationResult.error("Monthly limit must be greater than 0");
        } catch (NumberFormatException e) {
            return ValidationResult.error("Enter a valid amount");
        }
        return ValidationResult.success();
    }

    public void createBudget(String category, String limitText) {
        ValidationResult validation = validateBudgetInput(limitText);
        if (!validation.isSuccess()) {
            createBudgetState.setValue(Resource.error(validation.getErrorMessage()));
            return;
        }

        double limit = Double.parseDouble(limitText.trim());
        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setMonthlyLimit(limit);

        createBudgetState.setValue(Resource.loading());
        repository.createBudget(budget, new BudgetRepository.BudgetCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                createBudgetState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                createBudgetState.postValue(Resource.error(message));
            }
        });
    }

    public void loadBudgets() {
        // Automatically observed
    }

    public void deleteBudget(String budgetId) {
        deleteBudgetState.setValue(Resource.loading());
        repository.deleteBudget(budgetId, new BudgetRepository.BudgetCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                deleteBudgetState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                deleteBudgetState.postValue(Resource.error(message));
            }
        });
    }
}
