package com.example.paisapilot.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.paisapilot.model.Expense;
import com.example.paisapilot.model.MonthlyReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExportUtils {

    public static void exportReportToPDF(Context context, MonthlyReport report) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        
        int y = 50;
        
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("PaisaPilot - Monthly Report", 50, y, paint);
        
        y += 40;
        paint.setTextSize(18);
        canvas.drawText("Month: " + report.getMonthName(), 50, y, paint);
        
        y += 50;
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        canvas.drawText("Total Income: ₹" + String.format(Locale.getDefault(), "%.2f", report.getTotalIncome()), 50, y, paint);
        y += 25;
        canvas.drawText("Total Expenses: ₹" + String.format(Locale.getDefault(), "%.2f", report.getTotalExpenses()), 50, y, paint);
        y += 25;
        canvas.drawText("Total Savings: ₹" + String.format(Locale.getDefault(), "%.2f", report.getTotalSavings()), 50, y, paint);
        y += 25;
        canvas.drawText("Budget Usage: " + String.format(Locale.getDefault(), "%.1f%%", report.getBudgetUtilization()), 50, y, paint);
        
        y += 50;
        paint.setFakeBoldText(true);
        canvas.drawText("Transactions:", 50, y, paint);
        y += 30;
        
        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        
        canvas.drawText("Date", 50, y, paint);
        canvas.drawText("Title", 150, y, paint);
        canvas.drawText("Category", 350, y, paint);
        canvas.drawText("Amount", 500, y, paint);
        y += 10;
        canvas.drawLine(50, y, 550, y, paint);
        y += 20;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        
        for (Expense expense : report.getAllExpenses()) {
            if (y > 780) {
                document.finishPage(page);
                page = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageInfo.getPageNumber() + 1).create());
                canvas = page.getCanvas();
                y = 50;
            }
            
            String date = expense.getDate() != null ? sdf.format(expense.getDate().toDate()) : "-";
            canvas.drawText(date, 50, y, paint);
            canvas.drawText(truncate(expense.getTitle(), 25), 150, y, paint);
            canvas.drawText(expense.getCategory(), 350, y, paint);
            canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", expense.getAmount()), 500, y, paint);
            y += 20;
        }

        document.finishPage(page);

        File file = new File(context.getCacheDir(), "Monthly_Report_" + report.getMonthName().replace(" ", "_") + ".pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            shareFile(context, file, "application/pdf");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    public static void exportReportToCSV(Context context, MonthlyReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Title,Category,Amount,Payment Method,Note\n");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        for (Expense expense : report.getAllExpenses()) {
            csv.append(expense.getDate() != null ? sdf.format(expense.getDate().toDate()) : "").append(",");
            csv.append("\"").append(expense.getTitle().replace("\"", "\"\"")).append("\",");
            csv.append(expense.getCategory()).append(",");
            csv.append(expense.getAmount()).append(",");
            csv.append(expense.getPaymentMethod()).append(",");
            csv.append("\"").append(expense.getNote() != null ? expense.getNote().replace("\"", "\"\"") : "").append("\"\n");
        }

        File file = new File(context.getCacheDir(), "Expenses_" + report.getMonthName().replace(" ", "_") + ".csv");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(csv.toString().getBytes());
            shareFile(context, file, "text/csv");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private static void shareFile(Context context, File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Export Report"));
    }

    private static String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() <= length ? text : text.substring(0, length - 3) + "...";
    }
}
