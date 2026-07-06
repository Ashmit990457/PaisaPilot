package com.example.paisapilot.utils;

import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.*;
import com.example.paisapilot.model.*;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static Expense toModel(ExpenseEntity entity) {
        return new Expense(
                entity.getExpenseId(),
                entity.getTitle(),
                entity.getCategory(),
                entity.getAmount(),
                new Timestamp(entity.getDate()),
                entity.getNote(),
                entity.getPaymentMethod(),
                entity.getUserId(),
                new Timestamp(entity.getCreatedAt())
        );
    }

    public static List<Expense> toModelList(List<ExpenseEntity> entities) {
        List<Expense> list = new ArrayList<>();
        for (ExpenseEntity entity : entities) list.add(toModel(entity));
        return list;
    }

    public static ExpenseEntity toEntity(Expense model, SyncStatus status) {
        return new ExpenseEntity(
                model.getExpenseId(),
                model.getTitle(),
                model.getCategory(),
                model.getAmount(),
                model.getDate().toDate(),
                model.getNote(),
                model.getPaymentMethod(),
                model.getUserId(),
                model.getCreatedAt().toDate(),
                status
        );
    }

    public static Budget toModel(BudgetEntity entity) {
        return new Budget(
                entity.getBudgetId(),
                entity.getCategory(),
                entity.getMonthlyLimit(),
                entity.getSpentAmount(),
                entity.getRemainingAmount(),
                new Timestamp(entity.getCreatedAt()),
                entity.getUserId()
        );
    }

    public static List<Budget> toBudgetModelList(List<BudgetEntity> entities) {
        List<Budget> list = new ArrayList<>();
        for (BudgetEntity entity : entities) list.add(toModel(entity));
        return list;
    }

    public static BudgetEntity toEntity(Budget model, SyncStatus status) {
        return new BudgetEntity(
                model.getBudgetId(),
                model.getCategory(),
                model.getMonthlyLimit(),
                model.getSpentAmount(),
                model.getRemainingAmount(),
                model.getCreatedAt().toDate(),
                model.getUserId(),
                status
        );
    }

    public static SavingsGoal toModel(GoalEntity entity) {
        return new SavingsGoal(
                entity.getGoalId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getTargetAmount(),
                entity.getSavedAmount(),
                new Timestamp(entity.getTargetDate()),
                new Timestamp(entity.getCreatedAt()),
                entity.isCompleted()
        );
    }

    public static List<SavingsGoal> toGoalModelList(List<GoalEntity> entities) {
        List<SavingsGoal> list = new ArrayList<>();
        for (GoalEntity entity : entities) list.add(toModel(entity));
        return list;
    }

    public static GoalEntity toEntity(SavingsGoal model, SyncStatus status) {
        return new GoalEntity(
                model.getGoalId(),
                model.getUserId(),
                model.getTitle(),
                model.getTargetAmount(),
                model.getSavedAmount(),
                model.getTargetAmount() - model.getSavedAmount(),
                model.getTargetDate().toDate(),
                model.getCreatedAt().toDate(),
                model.isCompleted(),
                status
        );
    }

    public static RecurringExpense toModel(RecurringBillEntity entity) {
        RecurringExpense model = new RecurringExpense(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getCategory(),
                entity.getAmount(),
                entity.getFrequency(),
                new Timestamp(entity.getNextDueDate()),
                entity.isReminderEnabled(),
                entity.isAutoAddExpense()
        );
        if (entity.getLastProcessedDate() != null) {
            model.setLastProcessedDate(new Timestamp(entity.getLastProcessedDate()));
        }
        model.setCreatedAt(new Timestamp(entity.getCreatedAt()));
        return model;
    }

    public static List<RecurringExpense> toRecurringModelList(List<RecurringBillEntity> entities) {
        List<RecurringExpense> list = new ArrayList<>();
        for (RecurringBillEntity entity : entities) list.add(toModel(entity));
        return list;
    }

    public static RecurringBillEntity toEntity(RecurringExpense model, SyncStatus status) {
        return new RecurringBillEntity(
                model.getId(),
                model.getUserId(),
                model.getTitle(),
                model.getCategory(),
                model.getAmount(),
                model.getFrequency(),
                model.getNextDueDate().toDate(),
                model.getLastProcessedDate() != null ? model.getLastProcessedDate().toDate() : null,
                model.isReminderEnabled(),
                model.isAutoAddExpense(),
                model.getCreatedAt().toDate(),
                status
        );
    }

    public static UserProfile toModel(UserProfileEntity entity) {
        return new UserProfile(
                entity.getFullName(),
                entity.getOccupation(),
                entity.getCity(),
                entity.getMonthlyIncome(),
                entity.getMonthlySavingGoal(),
                entity.getCurrency(),
                entity.getSalaryCreditDate()
        );
    }

    public static UserProfileEntity toEntity(UserProfile model, String userId, SyncStatus status) {
        return new UserProfileEntity(
                userId,
                model.getFullName(),
                model.getOccupation(),
                model.getCity(),
                model.getMonthlyIncome(),
                model.getMonthlySavingGoal(),
                model.getCurrency(),
                model.getSalaryCreditDate(),
                status
        );
    }
}
