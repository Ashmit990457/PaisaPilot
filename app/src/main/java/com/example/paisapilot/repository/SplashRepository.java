package com.example.paisapilot.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.paisapilot.data.local.AppDatabase;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.data.local.entity.*;
import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.model.*;
import com.example.paisapilot.utils.Mapper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final AppDatabase database;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SplashRepository(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.database = AppDatabase.getInstance(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public interface Callback {
        void onResult(@NonNull NavigationTarget target);
        void onError(@NonNull String message);
    }

    public void checkAuthAndProfile(@NonNull final Callback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(NavigationTarget.LOGIN);
            return;
        }

        String uid = user.getUid();
        sessionManager.setLogin(uid);

        executor.execute(() -> {
            try {
                // Check if profile exists locally
                if (database.userProfileDao().getUserProfileSync(uid) != null) {
                    callback.onResult(NavigationTarget.MAIN);
                    return;
                }

                // If not locally, check remotely and download everything
                DocumentSnapshot profileDoc = Tasks.await(firestore.collection("users").document(uid).get());
                if (profileDoc.exists()) {
                    downloadAndCacheEverything(uid);
                    callback.onResult(NavigationTarget.MAIN);
                } else {
                    callback.onResult(NavigationTarget.PROFILE_SETUP);
                }
            } catch (Exception e) {
                callback.onError("Initialization failed: " + e.getMessage());
            }
        });
    }

    private void downloadAndCacheEverything(String uid) throws Exception {
        // Download all collections
        DocumentSnapshot profileDoc = Tasks.await(firestore.collection("users").document(uid).get());
        QuerySnapshot expensesSnap = Tasks.await(firestore.collection("users").document(uid).collection("expenses").get());
        QuerySnapshot budgetsSnap = Tasks.await(firestore.collection("users").document(uid).collection("budgets").get());
        QuerySnapshot goalsSnap = Tasks.await(firestore.collection("users").document(uid).collection("goals").get());
        QuerySnapshot recurringSnap = Tasks.await(firestore.collection("users").document(uid).collection("recurring").get());

        // Map and Save to Room
        UserProfile profile = new UserProfile(
                profileDoc.getString("fullName") != null ? profileDoc.getString("fullName") : "",
                profileDoc.getString("occupation") != null ? profileDoc.getString("occupation") : "",
                profileDoc.getString("city") != null ? profileDoc.getString("city") : "",
                profileDoc.getDouble("monthlyIncome") != null ? profileDoc.getDouble("monthlyIncome") : 0,
                profileDoc.getDouble("monthlySavingGoal") != null ? profileDoc.getDouble("monthlySavingGoal") : 0,
                profileDoc.getString("currency") != null ? profileDoc.getString("currency") : "₹",
                profileDoc.getLong("salaryCreditDate") != null ? profileDoc.getLong("salaryCreditDate").intValue() : 1
        );
        database.userProfileDao().insert(Mapper.toEntity(profile, uid, SyncStatus.SYNCED));

        List<ExpenseEntity> expenses = new ArrayList<>();
        for (DocumentSnapshot doc : expensesSnap.getDocuments()) {
            Expense m = doc.toObject(Expense.class);
            if (m != null) expenses.add(Mapper.toEntity(m, SyncStatus.SYNCED));
        }
        database.expenseDao().insertAll(expenses);

        List<BudgetEntity> budgets = new ArrayList<>();
        for (DocumentSnapshot doc : budgetsSnap.getDocuments()) {
            Budget m = doc.toObject(Budget.class);
            if (m != null) budgets.add(Mapper.toEntity(m, SyncStatus.SYNCED));
        }
        database.budgetDao().insertAll(budgets);

        List<GoalEntity> goals = new ArrayList<>();
        for (DocumentSnapshot doc : goalsSnap.getDocuments()) {
            SavingsGoal m = doc.toObject(SavingsGoal.class);
            if (m != null) goals.add(Mapper.toEntity(m, SyncStatus.SYNCED));
        }
        database.goalDao().insertAll(goals);

        List<RecurringBillEntity> bills = new ArrayList<>();
        for (DocumentSnapshot doc : recurringSnap.getDocuments()) {
            RecurringExpense m = doc.toObject(RecurringExpense.class);
            if (m != null) bills.add(Mapper.toEntity(m, SyncStatus.SYNCED));
        }
        database.recurringBillDao().insertAll(bills);
    }
}
