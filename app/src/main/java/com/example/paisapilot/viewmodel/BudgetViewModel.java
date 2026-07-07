package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private final MutableLiveData<Resource<Boolean>> budgetActionState = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private LiveData<List<Budget>> currentSource;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        this.repository = new BudgetRepository(application);
        refreshSource();
    }

    public LiveData<Resource<List<Budget>>> getBudgetsState() {
        return budgetsState;
    }

    public LiveData<Resource<Boolean>> getCreateBudgetState() {
        return budgetActionState;
    }

    public LiveData<Resource<Boolean>> getDeleteBudgetState() {
        return budgetActionState;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        refreshSource();
    }

    private void refreshSource() {
        if (currentSource != null) {
            budgetsState.removeSource(currentSource);
        }
        currentSource = repository.searchBudgets(searchQuery.getValue());
        budgetsState.addSource(currentSource, budgets -> {
            budgetsState.setValue(Resource.success(budgets));
        });
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
        saveBudget(null, category, limitText, 0);
    }

    public void saveBudget(@Nullable String id, String category, String limitText, double spentAmount) {
        ValidationResult validation = validateBudgetInput(limitText);
        if (!validation.isSuccess()) {
            budgetActionState.setValue(Resource.error(validation.getErrorMessage()));
            return;
        }

        double limit = Double.parseDouble(limitText.trim());
        Budget budget = new Budget();
        budget.setBudgetId(id);
        budget.setCategory(category);
        budget.setMonthlyLimit(limit);
        budget.setSpentAmount(spentAmount);

        budgetActionState.setValue(Resource.loading());
        BudgetRepository.BudgetCallback<Boolean> callback = new BudgetRepository.BudgetCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                budgetActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                budgetActionState.postValue(Resource.error(message));
            }
        };

        if (id == null) {
            repository.createBudget(budget, callback);
        } else {
            repository.updateBudget(budget, callback);
        }
    }

    public void loadBudgets() {
        refreshSource();
    }

    public void deleteBudget(String budgetId) {
        repository.deleteBudget(budgetId, new BudgetRepository.BudgetCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
            }

            @Override
            public void onError(@NonNull String message) {
                budgetActionState.postValue(Resource.error(message));
            }
        });
    }

    public void undoDelete(String budgetId) {
        repository.undoDelete(budgetId);
    }
}
