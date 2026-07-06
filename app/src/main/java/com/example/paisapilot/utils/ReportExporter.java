package com.example.paisapilot.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.paisapilot.model.Expense;
import com.example.paisapilot.model.MonthlyReport;
import com.example.paisapilot.model.SavingsGoal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportExporter {

    public static Uri exportToCSV(Context context, MonthlyReport report) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Title,Category,Amount,Payment Method\n");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (Expense e : report.getAllExpenses()) {
            csv.append(sdf.format(e.getDate().toDate())).append(",")
               .append(e.getTitle()).append(",")
               .append(e.getCategory()).append(",")
               .append(e.getAmount()).append(",")
               .append(e.getPaymentMethod()).append("\n");
        }

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Monthly_Report_" + System.currentTimeMillis() + ".csv");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csv.toString().getBytes());
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public static Uri exportToPDF(Context context, MonthlyReport report) throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 Size
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 50;

        // Title
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(24);
        canvas.drawText("PaisaPilot - Monthly Report", 50, y, paint);
        y += 40;

        // Date
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(14);
        canvas.drawText("Report for: " + report.getMonthName(), 50, y, paint);
        y += 40;

        // Summary
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Financial Summary", 50, y, paint);
        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Total Income: ₹" + report.getTotalIncome(), 50, y, paint);
        y += 20;
        canvas.drawText("Total Expenses: ₹" + report.getTotalExpenses(), 50, y, paint);
        y += 20;
        canvas.drawText("Total Savings: ₹" + report.getTotalSavings(), 50, y, paint);
        y += 20;
        canvas.drawText("Budget Utilization: " + String.format(Locale.getDefault(), "%.1f", report.getBudgetUtilization()) + "%", 50, y, paint);
        y += 40;

        // AI Summary
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("AI Insights", 50, y, paint);
        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(12);
        canvas.drawText(report.getAiSummary(), 50, y, paint);
        y += 50;

        // Goal Progress
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(14);
        canvas.drawText("Savings Goals", 50, y, paint);
        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        for (SavingsGoal goal : report.getGoalProgress()) {
            canvas.drawText(goal.getTitle() + ": " + goal.getPercentage() + "% completed", 70, y, paint);
            y += 20;
            if (y > 780) break; // Simple page break check
        }

        document.finishPage(page);

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Monthly_Report_" + System.currentTimeMillis() + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        }
        document.close();

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}
