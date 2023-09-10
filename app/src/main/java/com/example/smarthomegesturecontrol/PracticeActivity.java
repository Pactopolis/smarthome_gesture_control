package com.example.smarthomegesturecontrol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smarthomegesturecontrol.databinding.ActivityPracticeBinding;

public class PracticeActivity extends AppCompatActivity {
    private ActivityPracticeBinding binding;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPracticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bundle = getIntent().getExtras();

        // check for camera
        if (false == checkForCamera(this)) {
            Toast.makeText(this, "NO CAMERA", Toast.LENGTH_LONG);
        }
        else {
            // check for permissions
            if (false == hasCameraPermission()) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
            else {
                Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    private boolean checkForCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;
        else
            return false;
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Intent intent = new Intent(PracticeActivity.this, CameraActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(PracticeActivity.this, "NO PERMS", Toast.LENGTH_LONG);
                    }
                }
            }
    );

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }
}