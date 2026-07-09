package com.example.paisapilot.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.paisapilot.databinding.ActivityReceiptScannerBinding;
import com.example.paisapilot.model.ReceiptResult;
import com.example.paisapilot.utils.ImageCompressor;
import com.example.paisapilot.viewmodel.ReceiptViewModel;

public class ReceiptScannerActivity extends AppCompatActivity {

    private ActivityReceiptScannerBinding binding;
    private ReceiptViewModel viewModel;
    private Bitmap selectedBitmap;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    selectedBitmap = (Bitmap) extras.get("data");
                    updatePreview();
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedBitmap = ImageCompressor.compress(this, uri, 1920);
                    updatePreview();
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReceiptViewModel.class);

        binding.btnTakePhoto.setOnClickListener(v -> checkPermissionAndCamera());
        binding.btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnScan.setOnClickListener(v -> {
            if (selectedBitmap != null) viewModel.analyze(selectedBitmap);
        });

        observeViewModel();
        checkConfig();
    }

    private void checkConfig() {
        if (!viewModel.isApiKeyConfigured()) {
            new AlertDialog.Builder(this)
                    .setTitle("API Key Not Configured")
                    .setMessage("Gemini AI features require an API key. Please add GEMINI_API_KEY to your local.properties and sync the project.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void checkPermissionAndCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void updatePreview() {
        if (selectedBitmap != null) {
            binding.ivReceiptPreview.setImageBitmap(selectedBitmap);
            binding.ivReceiptPreview.setPadding(0, 0, 0, 0);
            binding.btnScan.setVisibility(View.VISIBLE);
        }
    }

    private void observeViewModel() {
        viewModel.getScanResult().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.layoutProcessing.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.layoutProcessing.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        navigateToPrefill(resource.getData());
                    }
                    break;
                case ERROR:
                    binding.layoutProcessing.setVisibility(View.GONE);
                    new AlertDialog.Builder(this)
                            .setTitle("AI Analysis Failed")
                            .setMessage(resource.getMessage())
                            .setPositiveButton("Retry", null)
                            .show();
                    break;
            }
        });
    }

    private void navigateToPrefill(ReceiptResult result) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("prefill_title", result.getMerchant() != null ? result.getMerchant() : result.getTitle());
        intent.putExtra("prefill_amount", String.valueOf(result.getAmount()));
        intent.putExtra("prefill_category", result.getCategory());
        intent.putExtra("prefill_payment", result.getPaymentMethod());
        intent.putExtra("prefill_date", result.getDate());
        intent.putExtra("confidence", result.getConfidence());
        
        startActivity(intent);
        finish();
    }
}
